/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiReferenceExpression
import com.wuhao.code.check.getPsiElementFactory

/**
 * 修复控制台使用非日志输出的方式
 * @author
 * @since
 */
class ConsolePrintFix: LocalQuickFix {

  override fun getFamilyName(): String {
    return "替换为日志输出"
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.endElement
    val factory = getPsiElementFactory(el)
    if (el.firstChild is PsiReferenceExpression) {
      if (el.firstChild.text.startsWith("System.out.print")) {
        el.firstChild.replace(factory.createExpressionFromText("LOG.info", null))
      } else if (el.firstChild.text.startsWith("System.err.print")) {
        el.firstChild.replace(factory.createExpressionFromText("LOG.error", null))
      }
    }
  }
}
