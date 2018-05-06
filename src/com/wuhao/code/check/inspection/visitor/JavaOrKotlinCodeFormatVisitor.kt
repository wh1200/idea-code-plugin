/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl
import com.wuhao.code.check.Messages
import com.wuhao.code.check.insertElementAfter
import com.wuhao.code.check.insertElementBefore
import com.wuhao.code.check.inspection.CodeFormatInspection
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.lexer.KtTokens.*
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * Created by 吴昊 on 18-4-26.
 */
open class JavaOrKotlinCodeFormatVisitor(holder: ProblemsHolder) : BaseCodeFormatVisitor(holder) {

  override fun support(language: Language): Boolean {
    return language == JavaLanguage.INSTANCE
        || language == KotlinLanguage.INSTANCE
  }

  override fun visitElement(element: PsiElement) {
    checkSpace(element)
    when (element) {
      is PsiFile -> {
        if (element.containingFile.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FILE) {
          holder.registerProblem(element, "文件长度不允许超过${CodeFormatInspection.MAX_LINES_PER_FILE}行", ERROR)
        }
      }
      is KtClass -> {
        classCommentChecker.checkKotlin(element)
      }
      is PsiClass -> {
        classCommentChecker.checkJava(element)
      }
      is KtObjectDeclaration -> {
        classCommentChecker.checkKotlin(element)
      }
    }
  }

  private fun checkSpace(element: PsiElement) {
    if (shouldHaveSpaceBothBeforeAndAfter(element)) {
      checkWhiteSpaceBothBeforeAndAfter(element)
    } else if (shouldOnlyHaveSpaceBefore(element)) {
      checkWhiteSpaceBefore(element)
    } else if (shouldOnlyHaveSpaceAfter(element)) {
      checkWhiteSpaceAfter(element)
    }
  }

  private fun shouldHaveSpaceBothBeforeAndAfter(element: PsiElement): Boolean {
    return (element is LeafPsiElement && element.elementType in shouldHaveSpaceBothBeforeAndAfterElementTypes)
        || (element is PsiKeyword && element.text in shouldHaveSpaceBothBeforeAndAfterKeywords)
        || (element is PsiJavaTokenImpl && element.text in shouldHaveSpaceBothBeforeAndAfterTokens)
  }

  private fun shouldOnlyHaveSpaceBefore(element: PsiElement): Boolean {
    // todo with element
    return false
  }

  private fun shouldOnlyHaveSpaceAfter(element: PsiElement): Boolean {
    return (element is LeafPsiElement && element.elementType in shouldOnlyHaveSpaceAfterElementTypes) || (element is PsiKeyword
        && element.text in shouldOnlyHaveSpaceAfterKeywords)
  }

  private fun checkWhiteSpaceBothBeforeAndAfter(element: PsiElement) {
    val keyword = element.text
    if (element.nextSibling !is PsiWhiteSpace || element.prevSibling !is PsiWhiteSpace) {
      holder.registerProblem(element, "$keyword 前后应当有空格", ERROR, SpaceQuickFix(SpaceQuickFix.Type.Both))
    }
  }

  private fun checkWhiteSpaceAfter(element: PsiElement) {
    val keyword = element.text
    if (element.nextSibling !is PsiWhiteSpace) {
      holder.registerProblem(element, "$keyword 之后应当有空格", ERROR, SpaceQuickFix(SpaceQuickFix.Type.After))
    }
  }

  private fun checkWhiteSpaceBefore(element: PsiElement) {
    val keyword = element.text
    if (element.prevSibling !is PsiWhiteSpace) {
      holder.registerProblem(element, "$keyword 之前应当有空格", ERROR, SpaceQuickFix(SpaceQuickFix.Type.Before))
    }
  }

  /**
   * 修复空格
   * @author 吴昊
   * @since 1.2.1
   */
  class SpaceQuickFix(private val type: Type) : LocalQuickFix {

    override fun getFamilyName(): String {
      return Messages.fixSpace
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val element = descriptor.psiElement
      val factory = KtPsiFactory(project)
      if (type in listOf(Type.Both, Type.After) && element.nextSibling !is PsiWhiteSpace) {
        element.insertElementAfter(factory.createWhiteSpace(" "))
      }
      if (type in listOf(Type.Both, Type.Before) && element.prevSibling !is PsiWhiteSpace) {
        element.insertElementBefore(factory.createWhiteSpace(" "))
      }
    }

    /**
     *
     * @author 吴昊
     * @since 1.2.1
     */
    enum class Type {
      Before, After, Both
    }
  }

  companion object {
    val shouldHaveSpaceBothBeforeAndAfterElementTypes = listOf(
        ELSE_KEYWORD, CATCH_KEYWORD, PLUSPLUS, MINUSMINUS, MUL, PLUS, MINUS, DIV, PERC,
        GT, LT, LTEQ, GTEQ, EQEQEQ, ARROW, DOUBLE_ARROW, EXCLEQEQEQ, EQEQ, EXCLEQ,
        EXCLEXCL, ANDAND, OROR, EQ, MULTEQ, DIVEQ, PERCEQ, PLUSEQ, MINUSEQ, NOT_IN, NOT_IS)
    val shouldHaveSpaceBothBeforeAndAfterKeywords = shouldHaveSpaceBothBeforeAndAfterElementTypes
        .map { it.value }
    val shouldHaveSpaceBothBeforeAndAfterTokens = listOf(">", "<", "=", ">=", "<=", "!=", "&&", "||", "&", "|", "==",
        "+", "-", "*", "/", "%", "+=", "-=", "/=", "*=", ">>", "<<", "<>")
    val shouldOnlyHaveSpaceAfterElementTypes = listOf(
        IF_KEYWORD, TRY_KEYWORD, DO_KEYWORD, WHILE_KEYWORD, WHEN_KEYWORD, ELVIS)
    val shouldOnlyHaveSpaceAfterKeywords = listOf("if", "for", "try",
        "while", "do", "switch")
  }
}
