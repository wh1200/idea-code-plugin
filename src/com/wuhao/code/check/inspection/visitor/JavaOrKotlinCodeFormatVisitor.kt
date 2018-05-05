/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.wuhao.code.check.inspection.CodeFormatInspection
import org.jetbrains.kotlin.idea.refactoring.getLineCount

/**
 * Created by 吴昊 on 18-4-26.
 */
open class JavaOrKotlinCodeFormatVisitor(holder: ProblemsHolder): BaseCodeFormatVisitor(holder) {

  override fun visitElement(element: PsiElement) {
    when (element) {
      is PsiFile -> {
        if (element.containingFile.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FILE) {
          holder.registerProblem(element, "文件长度不允许超过${CodeFormatInspection.MAX_LINES_PER_FILE}行", ProblemHighlightType.GENERIC_ERROR)
        }
      }
    }
  }

}
