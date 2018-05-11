/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix

import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.javascript.psi.impl.JSObjectLiteralExpressionImpl
import com.intellij.openapi.project.Project

/**
 * javascript对象属性排序
 * @author 吴昊
 * @since 1.1
 */
class JsPropertySortFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement as JSObjectLiteralExpressionImpl
    val sortedProperties = element.properties.sortedBy { it.name }
    element.properties.forEachIndexed { index, jsProperty ->
      jsProperty.replace(sortedProperties[index])
    }
    ReformatCodeProcessor(descriptor.psiElement.containingFile, true)
        .run()
  }

  override fun getFamilyName(): String {
    return "对象属性排序"
  }

}

