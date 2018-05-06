/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.JavaCodeStyleSettings
import com.wuhao.code.check.DEFAULT_CONTINUATION_INDENT_SPACE_COUNT
import com.wuhao.code.check.DEFAULT_INDENT_SPACE_COUNT
import com.wuhao.code.check.inspection.checker.ClassCommentChecker
import java.nio.charset.StandardCharsets

/**
 * Created by 吴昊 on 18-4-26.
 */
abstract class BaseCodeFormatVisitor(protected val holder: ProblemsHolder) {

  protected val classCommentChecker = ClassCommentChecker(holder)

  protected abstract fun visitElement(element: PsiElement)

  private fun checkIndent(element: PsiElement) {
    if (element is PsiFile) {
      val styleContainer = JavaCodeStyleSettings.getInstance(element.project)
          .container
      val indent = styleContainer.getIndentSize(element.fileType)
      val continuationIndent = styleContainer.getContinuationIndentSize(element.fileType)
      if (indent != DEFAULT_INDENT_SPACE_COUNT) {
        holder.registerProblem(element, "${element.fileType.name}文件的缩进必须为${DEFAULT_INDENT_SPACE_COUNT}个空格",
            ProblemHighlightType.ERROR)
      }
      if (continuationIndent != DEFAULT_CONTINUATION_INDENT_SPACE_COUNT) {
        holder.registerProblem(element, "${element.fileType.name}文件的持续缩进必须为${DEFAULT_CONTINUATION_INDENT_SPACE_COUNT}个空格",
            ProblemHighlightType.ERROR)
      }
    }

  }

  abstract fun support(language: Language): Boolean

  fun visit(element: PsiElement) {
    this.checkEncoding(element)
    this.checkIndent(element)
    this.visitElement(element)
  }

  private fun checkEncoding(element: PsiElement) {
    if (element is PsiFile && element.virtualFile != null && element.virtualFile.charset != StandardCharsets.UTF_8) {
      holder.registerProblem(element, "${element.name}的编码为${element.virtualFile.charset}，应该使用UTF-8",
          ProblemHighlightType.ERROR)
    }
  }

  companion object {
    const val MAX_TEMPLATE_LINES = 150
    const val DIRECTIVE_PREFIX = "v-"
    const val ACTION_PREFIX = "@"
    const val CUSTOM_ATTR_PREFIX = ":"
  }
}
