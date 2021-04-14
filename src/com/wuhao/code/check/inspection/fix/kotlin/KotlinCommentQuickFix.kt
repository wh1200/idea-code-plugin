/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix.kotlin

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.IncorrectOperationException
import com.wuhao.code.check.inspection.fix.java.JavaBlockCommentFix.Companion.BLOCK_COMMENT_END
import com.wuhao.code.check.inspection.fix.java.JavaBlockCommentFix.Companion.BLOCK_COMMENT_START
import com.wuhao.code.check.inspection.fix.java.JavaBlockCommentFix.Companion.BLOCK_COMMENT_STRING
import com.wuhao.code.check.inspection.fix.java.JavaBlockCommentFix.Companion.CLASS_COMMENT
import com.wuhao.code.check.ktPsiFactory
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration

/**
 * 构建kotlin注释元素
 * @param element
 * @return
 */
fun buildComment(element: PsiElement): PsiComment {
  val commentString = when (element) {
    is KtClass             -> CLASS_COMMENT
    is KtObjectDeclaration -> CLASS_COMMENT
    is KtFunction          -> buildFunctionComment(element)
    else                   -> BLOCK_COMMENT_STRING
  }
  return element.ktPsiFactory.createComment(commentString)
}

/**
 * 构建kotlin方法注释
 * @param element kotlin方法元素
 * @return
 */
private fun buildFunctionComment(element: KtFunction): String {
  val commentBuilder = StringBuilder("$BLOCK_COMMENT_START\n")
  element.valueParameterList?.parameters?.forEach {
    commentBuilder.append("* @param ${it.name}\n")
  }
  if (element.hasDeclaredReturnType()) {
    commentBuilder.append("* @return \n")
  }
  commentBuilder.append(BLOCK_COMMENT_END)
  return commentBuilder.toString()
}

/**
 * 注释修复
 * @author 吴昊
 * @since 1.1
 */
class KotlinCommentQuickFix : LocalQuickFix {

  companion object {
    private val LOG = Logger.getInstance("com.intellij.codeInspection.PropertyClassCreateInspection")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    try {
      val element = descriptor.psiElement
      val measureElement = if (element is LeafPsiElement) {
        element.parent
      } else {
        element
      }
      val comment = buildComment(measureElement)
      if (element is LeafPsiElement) {
        element.parent.addBefore(comment, element.parent.firstChild)
      } else {
        element.addBefore(comment, element.firstChild)
      }
    } catch (e: IncorrectOperationException) {
      LOG.error(e)
    }
  }

  override fun getFamilyName(): String {
    return name
  }

  override fun getName(): String {
    return "添加注释"
  }

}
