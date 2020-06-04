/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.javascript.TypeScriptJSXFileType
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionProperty
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.wuhao.code.check.constants.Messages.CONVERT_TO_VUE3_COMPONENT
import com.wuhao.code.check.insertElementsBefore
import com.wuhao.code.check.inspection.fix.ConvertToVue3Component.Companion.PROPS_PARAMETER_NAME
import com.wuhao.code.check.inspection.fix.VueComponentPropertySortFix.Companion.VUE3_LIFE_CYCLE_METHOD_MAP
import org.jetbrains.kotlin.backend.common.push
import org.jetbrains.kotlin.idea.core.replaced
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

/**
 * javascript对象属性排序
 * @author 吴昊
 * @since 1.1
 */
class ConvertToVue3Component : LocalQuickFix {

  companion object {
    const val PROPS_PARAMETER_NAME = "${'$'}props";
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement as JSObjectLiteralExpression
    val embeddedContent = element.parent.parent
    val imports = embeddedContent.getChildrenOfType<ES6ImportDeclaration>()
    val propertyMap = element.properties.associateBy { it.name }
    val nameProperty = propertyMap["name"]
    val mixinsProperty = propertyMap["mixins"]
    val propsProperty = propertyMap["props"]
    val allProperties = element.properties.toMutableList()
    val watchProperty = propertyMap["watch"]
    val computedProperty = propertyMap["computed"]
    val dataProperty = propertyMap["data"]
    val methodsProperty = propertyMap["methods"]
    val existsLifeCycleMethods = VueComponentPropertySortFix.LIFE_CYCLE_METHODS.mapNotNull { propertyMap[it] }
    val lifeCycleMethodsString = arrayListOf<String>()
    var beforeCreate = "";
    var created = "";
    val vueImportSpecifiers = arrayListOf(
        "ref",
        "defineComponent",
        "getCurrentInstance",
        "nextTick"
    )
    val propsNames = getPropNames(propsProperty);
    val watchString = buildWatchStr(watchProperty, propsNames)
    if (watchProperty != null && watchProperty.value is JSObjectLiteralExpression
        && (watchProperty.value as JSObjectLiteralExpression).properties.isEmpty()
    ) {
      allProperties.remove(watchProperty)
    }
    if (watchString.isNotBlank()) {
      vueImportSpecifiers.add("watch");
    }
    existsLifeCycleMethods.forEach {
      if (it is TypeScriptFunctionProperty) {
        when {
          VUE3_LIFE_CYCLE_METHOD_MAP.containsKey(it.name) -> {
            allProperties.remove(it)
            preHandleMethod(it, propsNames)
            val vue3HookName = VUE3_LIFE_CYCLE_METHOD_MAP[it.name]!!
            vueImportSpecifiers.add(vue3HookName)
            lifeCycleMethodsString.push(
                """$vue3HookName (() => 
                    | ${it.block!!.text})""".trimMargin()
            )
          }
          it.name == "beforeCreate"                       -> {
            allProperties.remove(it)
            preHandleMethod(it, propsNames)
            beforeCreate = it.block!!.children
                .toList().drop(1).dropLast(1)
                .joinToString("\n") { it.text }
          }
          it.name == "created"                            -> {
            allProperties.remove(it)
            preHandleMethod(it, propsNames)
            created = it.block!!.children
                .toList().drop(1).dropLast(1).joinToString("\n") { it.text }
          }
        }
      }
    }
    val renderProperty = propertyMap["render"]
    val mixinsString = mixinsProperty?.let { property ->
      (property.value as JSArrayLiteralExpression).expressions.joinToString(",") { it.text }
    }
    val importList = arrayListOf<String>()
    if (imports.none { it.fromClause != null && it.fromClause!!.referenceText == "Vue" }) {
      importList.add("import {${vueImportSpecifiers.joinToString(", ")}} from 'vue';")
    }
    val methodsStr = arrayListOf<String>()
    val setupReturn = arrayListOf<String>()
    var unrecognizedProperties = listOf<JSProperty>()
    if (methodsProperty != null && methodsProperty.value is JSObjectLiteralExpression) {
      val methods = methodsProperty.value as JSObjectLiteralExpression
      unrecognizedProperties = methods.properties.filter { it !is JSProperty && it !is TypeScriptFunctionProperty }
      methods.properties.forEach {
        if (it is TypeScriptFunctionProperty) {
          preHandleMethod(it, propsNames)
          methodsStr.add("const ${it.name} = ${it.parameterList!!.text} => ${it.block!!.text}")
          setupReturn.add(it.name!!)
        } else if (it is JSProperty) {
          if (it.nameIdentifier != it.value) {
            methodsStr.add("const ${it.name} = ${it.value?.text}")
          }
          setupReturn.add(it.name!!)
        }
      }
    }
    val hasUnRecognizedMethods = unrecognizedProperties.isNotEmpty()
    if (!hasUnRecognizedMethods) {
      allProperties.remove(methodsProperty)
    }
    var renderStr = ""
    if (renderProperty != null && renderProperty is TypeScriptFunctionProperty) {
      if (renderProperty.parameters.isNotEmpty() && renderProperty.parameters[0].name == "h") {
        renderProperty.parameters[0].delete()
      }
      allProperties.remove(renderProperty)
      renderStr = renderProperty.text
    }
//        val existsLifeCycleMethods = LIFE_CYCLE_METHODS.mapNotNull { propertyMap[it] }
//        val lifeCycleMethodsString = existsLifeCycleMethods.joinToString("\n") { "public " + it.text }
//        allProperties.removeAll(existsLifeCycleMethods)
    val propertiesStrList = arrayListOf<String>()
    allProperties.forEach {
      propertiesStrList.add(it.text)
    }
    propertiesStrList.add(
        """setup(${PROPS_PARAMETER_NAME}, {emit}) {
              | ${beforeCreate.trim()}
              | $watchString
              | ${methodsStr.joinToString(";\n")};
              | ${lifeCycleMethodsString.joinToString(";\n")}
              | ${created.trim()}
              | return {
              |   ${setupReturn.joinToString(",\n")}
              | };
              |}""".trimMargin()
    )
    if (renderStr.isNotBlank()) {
      propertiesStrList.add(renderStr)
    }
    val text = """defineComponent({
          |${propertiesStrList.joinToString(",\n")},
          |})""".trimMargin()
    val dummy = PsiFileFactory.getInstance(project).createFileFromText(
        "Dummy", TypeScriptJSXFileType.INSTANCE, text
    )
    val importStatement = JSPsiElementFactory.createJSSourceElement(
        "import {${vueImportSpecifiers.joinToString(", ")}} " +
            "from 'vue';",
        element
    )
    element.insertElementsBefore(*dummy.children)
    element.containingFile.firstChild.insertElementsBefore(importStatement)
    element.delete()
  }

  override fun getFamilyName(): String {
    return CONVERT_TO_VUE3_COMPONENT
  }

  private fun buildWatchStr(watchProperty: JSProperty?, propsNames: List<String>): String {
    val result = arrayListOf<String>()
    if (watchProperty == null) {
      return ""
    }
    if (watchProperty.value is JSObjectLiteralExpression) {
      val properties = (watchProperty.value as JSObjectLiteralExpression).properties
      properties.forEach { property ->
        if (property is TypeScriptFunctionProperty) {
          if (property.name in propsNames) {
            preHandleMethod(property)
            result.push(
                """watch(() => ${PROPS_PARAMETER_NAME}.${property.name}, ${property.parameterList!!.text} => 
              |${property.block!!.text})""".trimMargin()
            )
            property.delete()
          }
        }
      }
    }
    return result.joinToString(";\n") + ";"
  }

  private fun getPropNames(propsProperty: JSProperty?): List<String> {
    if (propsProperty == null) {
      return listOf()
    }
    if (propsProperty.value is JSObjectLiteralExpression) {
      return (propsProperty.value as JSObjectLiteralExpression).properties
          .map { it.name!! }
    }
    return listOf()
  }

  private fun preHandleMethod(
      it: TypeScriptFunctionProperty,
      propsNames: List<String>
  ) {
    VueMethodRecursiveVisitor(propsNames).visitElement(it)
  }

}

/**
 *
 * @author 吴昊
 * @since 0.1.15
 */
class VueMethodRecursiveVisitor(private val propsNames: List<String>) : JSElementVisitor() {

  val nameMap = mapOf(
      "this.${'$'}nextTick" to "nextTick",
      "this.${'$'}emit" to "emit",
      "this.${'$'}props" to PROPS_PARAMETER_NAME
  )

  override fun visitElement(element: PsiElement) {
    super.visitElement(element)
    if (element is JSReferenceExpression) {
      if (element.text in nameMap) {
        val newExp = JSPsiElementFactory.createJSExpression(nameMap[element.text]!!, element)
        element.replaced(newExp)
        return
      }
      if (element.text.startsWith("this.")) {
        val name = element.text.replace("this.", "")
        var newExp: Any? = null
        if (name in propsNames) {
          newExp = JSPsiElementFactory.createJSExpression(
              element.text.replace(
                  "this.",
                  "$PROPS_PARAMETER_NAME."
              ), element
          )
        } else {
          newExp = JSPsiElementFactory.createJSExpression(element.text.replace("this.", ""), element)
        }
        element.replaced(newExp)
        return
      }
    }
    element.children.forEach {
      it.accept(this)
    }
  }

}
