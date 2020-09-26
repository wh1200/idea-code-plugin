/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix.vue

import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.impl.JSObjectLiteralExpressionImpl
import com.intellij.openapi.project.Project

/**
 * javascript对象属性排序
 * @author 吴昊
 * @since 1.1
 */
class VueComponentPropertySortFix : LocalQuickFix {

  companion object {
    val VUE2_LIFE_CYCLE_METHODS = listOf(
        "beforeCreate",
        "created",
        "beforeMount",
        "mounted",
        "beforeUpdate",
        "updated",
        "activated",
        "deactivated",
        "beforeDestroy",
        "destroyed"
    )
    val VUE3_LIFE_CYCLE_METHOD_MAP = mapOf(
        "beforeMount" to "onBeforeMount",
        "mounted" to "onMounted",
        "beforeUpdate" to "onBeforeUpdate",
        "updated" to "onUpdated",
        "activated" to "onActivated",
        "deactivated" to "onDeactivated",
        "beforeDestroy" to "onBeforeUnmount",
        "destroyed" to "onUnmounted"
    )
    private val attrList = listOf("el", "name", "parent", "functional",
        "delimiters", "comments", "components", "directives", "filters",
        "extends", "mixins", "inheritAttrs", "model", "props", "propsData",
        "data", "computed", "watch", *VUE2_LIFE_CYCLE_METHODS.toTypedArray(), "methods", "template", "render",
        "renderError")

    fun sortVueComponentProperties(properties: Array<JSProperty>): ArrayList<JSProperty> {
      val copyProperties = properties.toMutableList()
      val sortedProperties = arrayListOf<JSProperty>()
      val propertyMap = copyProperties.associateBy { it.name }
      attrList.forEach {
        val sortProperty = propertyMap[it]
        if (sortProperty != null) {
          sortedProperties.add(sortProperty)
          copyProperties.remove(sortProperty)
        }
      }
      sortedProperties.addAll(copyProperties.sortedBy { it.name })
      return sortedProperties
    }
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement as JSObjectLiteralExpressionImpl
    val sortedProperties = sortVueComponentProperties(element.properties)
    element.properties.forEachIndexed { index, jsProperty ->
      jsProperty.replace(sortedProperties[index])
    }
    ReformatCodeProcessor(descriptor.psiElement.containingFile, true).run()
  }


  override fun getFamilyName(): String {
    return "Vue组件属性排序"
  }

}

