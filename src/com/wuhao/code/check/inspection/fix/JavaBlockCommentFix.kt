/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.impl.PsiElementFactoryImpl
import com.intellij.psi.impl.PsiManagerEx

/**
 * Created by 吴昊 on 18-4-26.
 * @author wuhao
 * @since
 */
class JavaBlockCommentFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val commentText = when (element) {
      is PsiClass -> CLASS_COMMENT
      is PsiMethod -> buildMethodComment(element)
      else -> BLOCK_COMMENT_STRING
    }
    val factory = PsiElementFactoryImpl(PsiManagerEx.getInstanceEx(element.project))
    val commentElement = factory.createCommentFromText(commentText, element)
    element.addBefore(commentElement, element.firstChild)
  }

  private fun buildMethodComment(element: PsiMethod): String {
    val commentBuilder = StringBuilder(BLOCK_COMMENT_START)
    element.parameterList.parameters.forEach {
      commentBuilder.append("* @param ${it.name}\n")
    }
    if (element.returnType != PsiPrimitiveType.VOID) {
      commentBuilder.append("* @return \n")
    }
    commentBuilder.append(BLOCK_COMMENT_END)
    return commentBuilder.toString()
  }

  override fun getFamilyName(): String {
    return "添加注释"
  }

  companion object {
    const val BLOCK_COMMENT_START = """/**
*
"""
    const val BLOCK_COMMENT_END = "*/"
    const val BLOCK_COMMENT_STRING = BLOCK_COMMENT_START + BLOCK_COMMENT_END
    const val CLASS_COMMENT = "$BLOCK_COMMENT_START* @author \n* @since \n$BLOCK_COMMENT_END"
  }
}
