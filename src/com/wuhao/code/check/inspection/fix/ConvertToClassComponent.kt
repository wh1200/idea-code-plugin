/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.TypeScriptJSXFileType
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionProperty
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.constants.Messages.CONVERT_TO_CLASS_COMPONENT
import com.wuhao.code.check.insertElementsBefore
import com.wuhao.code.check.inspection.fix.VueComponentPropertySortFix.Companion.LIFE_CYCLE_METHODS
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType

/**
 * javascript对象属性排序
 * @author 吴昊
 * @since 1.1
 */
class ConvertToClassComponent : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement as JSObjectLiteralExpression
    if (element.parent is ES6ExportDefaultAssignment) {
      val propertyMap = element.properties.associateBy { it.name }
      val nameProperty = propertyMap["name"]
      val mixinsProperty = propertyMap["mixins"]
      val propsProperty = propertyMap["props"]
      val allProperties = element.properties.toMutableList()
      val watchProperty = propertyMap["watch"]
      val computedProperty = propertyMap["computed"]
      val dataProperty = propertyMap["data"]
      val methodsProperty = propertyMap["methods"]
      val renderProperty = propertyMap["render"]
      if (nameProperty != null) {
        val mixinsString = mixinsProperty?.let { property ->
          (property.value as JSArrayLiteralExpression).expressions.joinToString(",") { it.text }
        }
        val hasMixins = mixinsString != null && mixinsString.isNotBlank()
        if (hasMixins) {
          allProperties.remove(mixinsProperty)
        }
        val decorators = arrayListOf<String>()
        val propsString = buildPropsString(propsProperty)
        if (!propsString.isNullOrBlank()) {
          decorators.add("Prop")
          allProperties.remove(propsProperty)
        }
        val methodsString = buildMethodsString(methodsProperty)
        if (!methodsString.isNullOrBlank()) {
          allProperties.remove(methodsProperty)
        }
        val dataString = buildDataString(dataProperty)
        if (!dataString.isNullOrBlank()) {
          allProperties.remove(dataProperty)
        }
        val watchString = buildWatchString(watchProperty)
        if (!watchString.isNullOrBlank()) {
          decorators.add("Watch")
          allProperties.remove(watchProperty)
        }
        val computedString = buildComputedString(computedProperty)
        if (computedString.isNotBlank()) {
          allProperties.remove(computedProperty)
        }
        val importList = arrayListOf<String>()
        if (!hasMixins) {
          importList.add("import Vue from 'vue';")
        }
        if (decorators.isNotEmpty()) {
          importList.add("import {${decorators.joinToString(", ")}} from 'vue-property-decorator';")
        }
        if (hasMixins) {
          importList.add("import Component, {mixins} from 'vue-class-component';")
        } else {
          importList.add("import Component from 'vue-class-component';")
        }
        val extendsExpression = if (mixinsString == null) {
          "Vue"
        } else {
          "mixins($mixinsString)"
        }
        val renderString = "public " + renderProperty?.text
        if (renderProperty != null) {
          allProperties.remove(renderProperty)
        }
        val existsLifeCycleMethods = LIFE_CYCLE_METHODS.mapNotNull { propertyMap[it] }
        val lifeCycleMethodsString = existsLifeCycleMethods.joinToString("\n") { "public " + it.text }
        allProperties.removeAll(existsLifeCycleMethods)
        val body = listOfNotNull(
            propsString, dataString,
            computedString, watchString,
            lifeCycleMethodsString, methodsString,
            renderString
        )
        val text = """
          |${importList.joinToString("\n")}
          |
          |@Component({
          | ${allProperties.joinToString(",\n") { it.text }}
          |})
          |export default class ${(nameProperty.value as JSLiteralExpression?)?.stringValue} extends $extendsExpression {
          | ${body.joinToString("\n")}
        }""".trimMargin()
        val dummy = PsiFileFactory.getInstance(project).createFileFromText(
            "Dummy", TypeScriptJSXFileType.INSTANCE, text)
        element.parent.insertElementsBefore(*dummy.children)
        element.parent.delete()
      }
    }
  }

  override fun getFamilyName(): String {
    return CONVERT_TO_CLASS_COMPONENT
  }

  private fun buildComputedString(computedProperty: JSProperty?): String {
    return if (computedProperty != null) {
      val obj = computedProperty.value as JSObjectLiteralExpression
      obj.properties.joinToString("\n") {
        "get ${it.text}"
      }
    } else {
      ""
    }
  }

  private fun buildDataString(dataProperty: JSProperty?): String? {
    if (dataProperty != null) {
      if (dataProperty is TypeScriptFunctionProperty) {
        if (isPureReturn(dataProperty)) {
          val returnObject = findReturnObject(dataProperty)
          return returnObject?.properties?.joinToString("\n") {
            "public ${it.name} = ${it.value?.text};"
          }
        }
      } else {
        val exp = dataProperty.value!!.getChildOfType<JSParenthesizedExpression>()!!
        val obj = exp.getChildOfType<JSObjectLiteralExpression>()
        return obj?.properties?.joinToString("\n") {
          "public ${it.name} = ${it.value?.text};"
        }
      }
    }
    return null
  }

  private fun buildMethodsString(methodsProperty: JSProperty?): String? {
    if (methodsProperty != null) {
      val methods = methodsProperty.value as JSObjectLiteralExpression
      return methods.properties.joinToString("\n") {
        "public " + it.text
      }
    }
    return null
  }

  private fun buildPropsString(propsProperty: JSProperty?): String? {
    if (propsProperty != null) {
      val props = propsProperty.value
      if (props is JSObjectLiteralExpression) {
        return props.properties.joinToString("\n") {
          val typeString = resolveTypeOfProp(it)
          val tsType = when (typeString) {
            "String"  -> "string"
            "Number"  -> "number"
            "Boolean" -> "boolean"
            "Array"   -> "any[]"
            else      -> "any"
          }
          """@Prop(${it.value!!.text})
                    |public ${it.name}: $tsType;
                  """.trimMargin()
        }
      }
    }
    return null
  }

  private fun buildWatchString(watchProperty: JSProperty?): String? {
    if (watchProperty != null) {
      val watchProperties = (watchProperty.value as JSObjectLiteralExpression).properties
      if (watchProperties.any { it.value is TypeScriptFunctionExpression }) {
        return null
      }
      return watchProperties.joinToString("\n") { property ->
        val name = property.name
        val nameText = property.nameIdentifier!!.text
        val newName = if (nameText.isNotBlank() && nameText[0] in listOf('\'', '"')) {
          "${property.name?.split('.')?.last()}Changed"
        } else {
          "${property.name}Changed"
        }
        if (property is TypeScriptFunctionProperty) {
          property.setName(newName)
          """
              @Watch('$name')
              public ${property.text}
            """.trimIndent()
        } else if (property is JSProperty) {
          val value = property.value as JSObjectLiteralExpression
          val handler = value.findProperty("handler")
          val handlerText = if (handler is TypeScriptFunctionProperty) {
            handler.setName(newName)
            handler.text
          } else if (handler is JSProperty) {
            if (handler.value is TypeScriptFunctionExpression) {
              """$newName = ${handler.value?.text}""".trimMargin()
            } else {
              """$newName(value){
              | this.${handler.value?.text}(value);
              |}""".trimMargin()
            }
          } else {
            handler!!.text
          }
          val options = value.properties.filter { it.name == "deep" || it.name == "immediate" }
              .joinToString(",\n") { it.text }
          """
            @Watch('$name', {
              $options
            })
            public $handlerText
          """.trimIndent()
        } else {
          """
              @Watch
              public ${property.text}
            """.trimIndent()
        }

      }
    }
    return null
  }

  private fun findReturnObject(dataProperty: TypeScriptFunctionProperty): JSObjectLiteralExpression? {
    dataProperty.getChildOfType<JSBlockStatement>()?.let {
      val returnStatement = it.getChildOfType<JSReturnStatement>()
      if (returnStatement != null) {
        return returnStatement.getChildOfType()
      }
    }
    return null
  }

  private fun isPureReturn(dataProperty: TypeScriptFunctionProperty): Boolean {
    val body = dataProperty.getChildOfType<JSBlockStatement>()
    if (body != null) {
      return body.children.filter {
        it !is PsiWhiteSpace && it !is LeafPsiElement
      }.size == 1
    }
    return false
  }

  private fun resolveTypeOfProp(property: JSProperty): String? {
    val value = property.value
    if (value is JSObjectLiteralExpression) {
      val typeProperty = value.findProperty("type")
      return typeProperty?.value?.text
    } else {
      return value?.text
    }
  }

}
