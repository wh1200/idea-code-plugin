/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.JavaCodeStyleSettings
import com.wuhao.code.check.DEFAULT_CONTINUATION_INDENT_SPACE_COUNT
import com.wuhao.code.check.DEFAULT_INDENT_SPACE_COUNT
import com.wuhao.code.check.inspection.checker.ClassCommentChecker

/**
 * Created by 吴昊 on 18-4-26.
 */
abstract class BaseCodeFormatVisitor(protected val holder: ProblemsHolder) {

  protected val classCommentChecker = ClassCommentChecker(holder)

  abstract fun visitElement(element: PsiElement)

  fun checkIndent(file: PsiFile, fileType: FileType) {
    val styleContainer = JavaCodeStyleSettings.getInstance(file.project)
        .container
    val indent = styleContainer.getIndentSize(fileType)
    val continuationIndent = styleContainer.getContinuationIndentSize(fileType)
    if (indent != DEFAULT_INDENT_SPACE_COUNT) {
      holder.registerProblem(file, "${file.fileType.name}文件的缩进必须为${DEFAULT_INDENT_SPACE_COUNT}个空格",
          ProblemHighlightType.ERROR)
    }
    if (continuationIndent != DEFAULT_CONTINUATION_INDENT_SPACE_COUNT) {
      holder.registerProblem(file, "${file.fileType.name}文件的持续缩进必须为${DEFAULT_CONTINUATION_INDENT_SPACE_COUNT}个空格",
          ProblemHighlightType.ERROR)
    }
  }
}
