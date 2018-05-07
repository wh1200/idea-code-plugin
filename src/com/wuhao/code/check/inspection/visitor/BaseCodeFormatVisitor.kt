/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.application.options.CodeStyle
import com.intellij.application.options.JavaCodeStyleSettingsProvider
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.openapi.options.newEditor.SettingsDialogFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.JavaCodeStyleSettings
import com.wuhao.code.check.DEFAULT_CONTINUATION_INDENT_SPACE_COUNT
import com.wuhao.code.check.DEFAULT_INDENT_SPACE_COUNT
import com.wuhao.code.check.inspection.checker.ClassCommentChecker
import java.nio.charset.StandardCharsets

/**
 * 基本的代码格式检查访问器，主要检查了文件缩进及文件编码
 * 文件缩进强制为2个空格，持续缩进为4个空格，文件编码为UTF-8
 *
 * Created by 吴昊 on 18-4-26.
 * @author 吴昊
 * @since 1.1
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
            ProblemHighlightType.ERROR, object : LocalQuickFix {
          override fun getFamilyName(): String {
            return "打开设置"
          }

          override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            SettingsDialogFactory.getInstance().create(project, "", JavaCodeStyleSettingsProvider().createSettingsPage(
                CodeStyle.getDefaultSettings(), CodeStyle.getDefaultSettings()
            ), true, false).show()
          }
        })
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
