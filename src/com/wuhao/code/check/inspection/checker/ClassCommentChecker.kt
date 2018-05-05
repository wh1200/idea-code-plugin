/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.checker

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiClass
import com.intellij.psi.javadoc.PsiDocComment
import com.wuhao.code.check.inspection.fix.JavaBlockCommentFix
import com.wuhao.code.check.inspection.fix.KotlinCommentQuickFix
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtClass

/**
 * Created by 吴昊 on 18-4-26.
 */
class ClassCommentChecker(holder: ProblemsHolder) : BaseChecker(holder) {


  fun checkJava(element: PsiClass) {
    if (element.firstChild == null || element.firstChild !is PsiDocComment) {
      holder.registerProblem(element, "缺少类注释", ProblemHighlightType.GENERIC_ERROR, JavaBlockCommentFix())
    }
  }

  fun checkKotlin(element: KtClass) {
    if (element.firstChild == null || element.firstChild !is KDoc) {
      holder.registerProblem(element, "缺少类注释", ProblemHighlightType.GENERIC_ERROR, KotlinCommentQuickFix())
    }
  }

}
