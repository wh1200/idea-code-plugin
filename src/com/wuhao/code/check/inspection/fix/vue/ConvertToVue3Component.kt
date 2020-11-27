/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix.vue

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.javascript.TypeScriptJSXFileType
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSParenthesizedExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionProperty
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.wuhao.code.check.ancestorOfType
import com.wuhao.code.check.constants.Messages.CONVERT_TO_VUE3_COMPONENT
import com.wuhao.code.check.insertElementsBefore
import com.wuhao.code.check.inspection.fix.vue.VueComponentPropertySortFix.Companion.VUE2_LIFE_CYCLE_METHODS
import com.wuhao.code.check.inspection.fix.vue.VueComponentPropertySortFix.Companion.VUE3_LIFE_CYCLE_METHOD_MAP
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

val PsiElement.jsDocComment: JSDocComment?
  get() {
    return if (this.firstChild is JSDocComment) {
      return this.firstChild as JSDocComment
    } else {
      null
    }
  }

/**
 * javascript对象属性排序
 * @author 吴昊
 * @since 1.1
 */
class ConvertToVue3Component : LocalQuickFix {

  companion object {
    const val PROPS_PARAMETER_NAME = "props"
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement as JSObjectLiteralExpression
    val embeddedContent = element.parent.parent
    val imports = embeddedContent.getChildrenOfType<ES6ImportDeclaration>()
    val propertyMap = element.properties.associateBy { it.name }
    val mixinsProperty = propertyMap["mixins"]
    val propsProperty = propertyMap["props"]
    val allProperties = element.properties.toMutableList()
    val watchProperty = propertyMap["watch"]
    val computedProperty = propertyMap["computed"]
    val dataProperty = propertyMap["data"]
    val methodsProperty = propertyMap["methods"]
    val vueImportSpecifiers = hashSetOf(
        "ref",
        "defineComponent",
        "getCurrentInstance",
        "nextTick"
    )
    val propNames = resolvePropNames(propsProperty)
    val refValueNames = resolveRefNames(dataProperty)
    val injectionNames = arrayListOf<String>()
    val methodNames = resolveMethodNames(methodsProperty)
    val reactiveValueNames = resolveReactiveValueNames(dataProperty)
    val computedNames = resolveComputedNames(computedProperty)
    val methodVisitor = VueMethodRecursiveVisitor(propNames,
        refValueNames, methodNames, computedNames, reactiveValueNames
    )
    val watchList = resolveWatchList(watchProperty, propNames, methodVisitor)
    val valueList: ArrayList<ValueExpression> =
        resolveValues(dataProperty)
    val functionList: List<FunctionExpression> = resolveFunctionList(
        methodsProperty, methodVisitor, valueList
    )
    if (watchProperty != null && watchProperty.value is JSObjectLiteralExpression
        && (watchProperty.value as JSObjectLiteralExpression).properties.isEmpty()
    ) {
      allProperties.remove(watchProperty)
    }
    if (watchList.isNotEmpty()) {
      vueImportSpecifiers.add("watch");
    }
    val computedList: ArrayList<ComputedExpression> = resolveComputedList(computedProperty, methodVisitor)
    if (computedProperty != null && (computedProperty.value as JSObjectLiteralExpression).properties.isEmpty()) {
      computedProperty.delete()
    }
    if (watchProperty != null && (watchProperty.value as JSObjectLiteralExpression).properties.isEmpty()) {
      watchProperty.delete()
    }
    if (methodVisitor.refs.isNotEmpty()) {
      methodVisitor.refs.forEach {
        if (valueList.none { it.name == "${it}Ref" }) {
          valueList.add(ValueExpression().apply {
            name = "${it}Ref"
            initializer = "ref(null)"
          })
        }
      }
    }
    val renderProperty = propertyMap["render"]
    val importList = arrayListOf<String>()
    if (imports.none { it.fromClause != null && it.fromClause!!.referenceText == "Vue" }) {
      importList.add("import {${vueImportSpecifiers.joinToString(", ")}} from 'vue';")
    }
    if (renderProperty != null && renderProperty is TypeScriptFunctionProperty) {
      if (renderProperty.parameters.isNotEmpty() && renderProperty.parameters[0].name == "h") {
        renderProperty.parameters[0].delete()
      }
      allProperties.remove(renderProperty)
    }
    val lifeCycleExpressions: List<LifecycleExpression> = resolveLifeCycelFunctions(propertyMap, methodVisitor)
    val componentProps = arrayListOf<String>()
    allProperties.forEach {
      componentProps.add(it.text)
    }
    val otherSetupExpressions = listOf<String>()
    val text = createCompositionApiCode(valueList,
        watchList, computedList, functionList,
        lifeCycleExpressions, componentProps, "",
        "", otherSetupExpressions, renderProperty?.text ?: "")
    val dummy = PsiFileFactory.getInstance(project).createFileFromText(
        "Dummy", TypeScriptJSXFileType.INSTANCE, text
    )
    vueImportSpecifiers.add("inject")
    vueImportSpecifiers.add("provide")
    vueImportSpecifiers.add("ref")
    vueImportSpecifiers.add("Ref")
    vueImportSpecifiers.add("reactive")
    vueImportSpecifiers.add("computed")
    vueImportSpecifiers.add("nextTick")
    vueImportSpecifiers.addAll(listOf("PropType", "defineComponent"))
    val importStatement = JSPsiElementFactory.createJSSourceElement(
        "import {${vueImportSpecifiers.joinToString(", ")}} " +
            "from 'vue';",
        element
    )
    val embedContent = element.ancestorOfType<JSEmbeddedContent>()
    if (embedContent != null) {
      embedContent.firstChild.insertElementsBefore(importStatement)
    } else {
      element.containingFile.firstChild.insertElementsBefore(importStatement)
    }
    element.insertElementsBefore(*dummy.children)
    element.delete()
  }

  override fun getFamilyName(): String {
    return CONVERT_TO_VUE3_COMPONENT
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

  private fun resolveComputedList(computedProperty: JSProperty?, methodVisitor: VueMethodRecursiveVisitor): ArrayList<ComputedExpression> {
    val res = arrayListOf<ComputedExpression>()
    if (computedProperty == null) {
      return res
    }
    if (computedProperty.value is JSObjectLiteralExpression) {
      val computedObj = computedProperty.value as JSObjectLiteralExpression
      computedObj.properties.forEach {
        if (it is TypeScriptFunctionProperty) {
          val computed = ComputedExpression()
          computed.comment = it.jsDocComment?.text ?: ""
          computed.name = it.name ?: ""
          methodVisitor.visitElement(it.block!!)
          computed.expression = it.block?.text ?: "{}"
          res.add(computed)
          it.delete()
        }
      }
    }
    return res;
  }

  private fun resolveComputedNames(computedProperty: JSProperty?): List<String> {
    if (computedProperty == null) {
      return listOf()
    }
    return (computedProperty.value as JSObjectLiteralExpression).properties.map { it.name!! }
  }

  private fun resolveDataNames(dataProperty: JSProperty, isobj: Boolean): List<String> {
    if (dataProperty is TypeScriptFunctionProperty) {
      if (isPureReturn(dataProperty)) {
        val returnObject = findReturnObject(dataProperty)
        return returnObject?.properties?.map { it.name!! } ?: listOf()
      }
    } else {
      val exp = dataProperty.value!!.getChildOfType<JSParenthesizedExpression>()!!
      val obj = exp.getChildOfType<JSObjectLiteralExpression>()
      return obj?.properties?.filter {
        if (isobj) {
          it.initializer is JSObjectLiteralExpression
        } else {
          it.initializer !is JSObjectLiteralExpression
        }
      }?.map { it.name!! } ?: listOf()
    }
    return listOf()
  }

  private fun resolveFunctionList(methodsProperty: JSProperty?,
                                  methodVisitor: VueMethodRecursiveVisitor, valueList: ArrayList<ValueExpression>): List<FunctionExpression> {
    if (methodsProperty == null) {
      return listOf()
    }
    val result = arrayListOf<FunctionExpression>()
    // methods
    (methodsProperty.value as JSObjectLiteralExpression).properties.forEach { property ->
      if (property is TypeScriptFunctionProperty) {
        val fn = FunctionExpression()
        fn.comment = property.jsDocComment?.text ?: ""
        fn.name = property.name ?: ""
        fn.argumentsExpression = property.parameterList?.text ?: "()"
        methodVisitor.visitElement(property.block!!)
        fn.bodyExpression = property.block?.text ?: "{}"
        result.add(fn)
        property.delete()
      }
    }
    return result
  }

  private fun resolveLifeCycelFunctions(propertyMap: Map<String?, JSProperty>, methodVisitor: VueMethodRecursiveVisitor): List<LifecycleExpression> {
    val result = arrayListOf<LifecycleExpression>()
    propertyMap.filter {
      it.key in VUE2_LIFE_CYCLE_METHODS
    }.forEach { entry ->
      val fnProperty = entry.value as TypeScriptFunctionProperty
      methodVisitor.visitElement(fnProperty.block!!)
      val fn = LifecycleExpression()
      fn.name = VUE3_LIFE_CYCLE_METHOD_MAP[entry.key]
      fn.comment = entry.value.jsDocComment?.text ?: ""
      fn.bodyExpression = fnProperty.block?.text ?: "{}"
      result.add(fn)
      fnProperty.delete()
    }
    return result
  }

  private fun resolveMethodNames(methodsProperty: JSProperty?): List<String> {
    if (methodsProperty == null) {
      return listOf()
    }
    val methods = methodsProperty.value
    if (methods is JSObjectLiteralExpression) {
      return methods.properties.map { it.name!! }
    }
    return listOf()
  }

  private fun resolvePropNames(propsProperty: JSProperty?): List<String> {
    if (propsProperty == null) {
      return listOf()
    }
    val props = propsProperty.value
    if (props is JSObjectLiteralExpression) {
      return props.properties.map { it.name!! }
    }
    return listOf()
  }

  private fun resolveReactiveValueNames(dataProperty: JSProperty?): List<String> {
    if (dataProperty == null) {
      return listOf()
    }
    return resolveDataNames(dataProperty, true)
  }

  private fun resolveRefNames(dataProperty: JSProperty?): List<String> {
    if (dataProperty == null) {
      return listOf()
    }
    return resolveDataNames(dataProperty, false)
  }

  private fun resolveValues(dataProperty: JSProperty?): ArrayList<ValueExpression> {
    if (dataProperty == null) {
      return arrayListOf()
    }
    val valueList = arrayListOf<ValueExpression>()
    if (dataProperty is TypeScriptFunctionProperty) {
      if (isPureReturn(dataProperty)) {
        val returnObject = findReturnObject(dataProperty)
        if (returnObject != null) {
          returnObject.properties.forEach {
            val valueExp = ValueExpression()
            valueExp.name = it.name!!
            // 不是prop
            valueExp.initializer = it.initializer?.text
            if (it.initializer is JSObjectLiteralExpression) {
              valueExp.initializer = "reactive(${valueExp.initializer})"
            } else {
              if (!valueExp.type.isNullOrBlank()) {
                valueExp.type = "Ref<${valueExp.type}>"
              }
              if (valueExp.initializer.isNullOrBlank()) {
                valueExp.initializer = "ref(null)"
              } else {
                valueExp.initializer = "ref(${valueExp.initializer})"
              }
            }
            valueList.add(valueExp)
            it.delete()
          }
          if (valueList.size == returnObject.properties.size || returnObject.properties.isEmpty()) {
            dataProperty.delete()
          }
        }
      }
    }
    return valueList
  }

  private fun resolveWatchList(watchProperty: JSProperty?, propNames: List<String>,
                               visitor: VueMethodRecursiveVisitor): List<WatchExpression> {
    val result = arrayListOf<WatchExpression>()
    if (watchProperty == null) {
      return listOf()
    }
    if (watchProperty.value is JSObjectLiteralExpression) {
      val properties = (watchProperty.value as JSObjectLiteralExpression).properties
      properties.forEach { property ->
        val we = WatchExpression()
        we.comment = property.jsDocComment?.text ?: ""
        val watchProp = property.name
        if (watchProp in propNames) {
          we.watchExpression = "${PROPS_PARAMETER_NAME}.$watchProp"
        } else {
          we.watchExpression = "$watchProp"
        }
        if (property is TypeScriptFunctionProperty) {
          visitor.visitElement(property.block!!)
        }
        if (property is TypeScriptFunctionProperty) {
          we.callbackExpression = " ${property.parameterList?.text ?: "()"} => ${property.block?.text}"
        } else if (property.value is JSObjectLiteralExpression) {
          val obj = property.value as JSObjectLiteralExpression
          obj.properties.find { it.name == "handler" }.apply {
            if (this is TypeScriptFunctionProperty) {
              visitor.visitElement(this.block!!)
              we.callbackExpression = " ${(this.value as TypeScriptFunctionExpression).parameterList?.text ?: "()"} => ${(this.value as TypeScriptFunctionExpression).block?.text}"
              this.delete()
            }
          }
          if (obj.properties.size > 1) {
            we.optionsExpression = obj.text
          }
        }
        property.delete()
        result.add(we)
      }
    }
    return result
  }

}
