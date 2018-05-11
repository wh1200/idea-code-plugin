/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.checker

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiTypeParameter
import com.intellij.psi.javadoc.PsiDocComment
import com.wuhao.code.check.Messages.classCommentRequired
import com.wuhao.code.check.inspection.fix.JavaBlockCommentFix
import com.wuhao.code.check.inspection.fix.kotlin.KotlinCommentQuickFix
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtObjectDeclaration

/**
 * Created by 吴昊 on 18-4-26.
 */
class ClassCommentChecker(holder: ProblemsHolder) : BaseChecker(holder) {

  fun checkJava(element: PsiClass) {
    if ((element !is PsiTypeParameter && element.firstChild == null || element.firstChild !is PsiDocComment) && element !is PsiAnonymousClass) {
      if (element.nameIdentifier != null) {
        registerProblem(element.nameIdentifier!!, JavaBlockCommentFix())
      } else {
        registerProblem(element, JavaBlockCommentFix())
      }
    }
  }

  fun checkKotlin(element: KtClass) {
    if (element !is KtEnumEntry && element.nameIdentifier != null) {
      checkKotlinClassComment(element, element.nameIdentifier)
    }
  }

  fun checkKotlin(element: KtObjectDeclaration) {
    if (!element.isCompanion() && element.nameIdentifier != null) {
      checkKotlinClassComment(element, element.nameIdentifier)
    }
  }

  private fun checkKotlinClassComment(element: PsiElement, nameIdentifier: PsiElement?) {
    if (element.firstChild == null || element.firstChild !is KDoc) {
      if (nameIdentifier != null) {
        registerProblem(nameIdentifier, KotlinCommentQuickFix())
      } else {
        registerProblem(element, KotlinCommentQuickFix())
      }
    }
  }

  private fun registerProblem(element: PsiElement, fix: LocalQuickFix) {
    holder.registerProblem(element, classCommentRequired, ERROR, fix)
  }

}

