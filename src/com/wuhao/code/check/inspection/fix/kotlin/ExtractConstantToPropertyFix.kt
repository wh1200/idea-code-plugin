/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix.kotlin

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.wuhao.code.check.*
import org.jetbrains.kotlin.psi.*

/**
 * 将常量参数导出为变量
 * @author 吴昊
 * @since 1.3.4
 */
class ExtractConstantToPropertyFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val constant = descriptor.psiElement as KtConstantExpression
    //获取包含数值参数的当前表达式
    val exp = descriptor.psiElement.getAncestor(2)!!
        .getContinuousAncestorsMatches<KtExpression> {
          it is KtCallExpression
              || it is KtQualifiedExpression || it is KtProperty
        }.last()
    val propertyName = PROPERTY_NAME_PLACEHOLDER
    val factory = KtPsiFactory(project)
    val property = factory.createProperty("val $propertyName = ${constant.text}")
    val newProperty = property.insertBefore(exp)
    val newValueArgument = constant.parent
        .replace(factory.createArgument(propertyName))
    newProperty.insertElementAfter(getNewLine(project))
    renameElement(newValueArgument)
  }

  override fun getFamilyName(): String {
    return "提取为变量"
  }

}

