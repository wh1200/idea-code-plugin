/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassImpl
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.inspection.CodeFormatInspection
import com.wuhao.code.check.inspection.checker.ClassCommentChecker
import com.wuhao.code.check.inspection.fix.SpaceQuickFix
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.lexer.KtTokens.*
import org.jetbrains.kotlin.psi.*

/**
 * java和kotlin共同的代码格式检查访问器
 * 主要检查文件长度和类注释
 * Created by 吴昊 on 18-4-26.
 * @author 吴昊
 * @since 1.1
 */
open class JavaOrKotlinCodeFormatVisitor(val holder: ProblemsHolder)
  : BaseCodeFormatVisitor, PsiElementVisitor() {

  private val classCommentChecker = ClassCommentChecker(holder)

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
      is PsiClassImpl -> {
        classCommentChecker.checkJava(element)
      }
      is KtObjectDeclaration -> {
        classCommentChecker.checkKotlin(element)
      }
    }
  }

  private fun checkSpace(element: PsiElement) {
    when {
      needSpaceBothBeforeAndAfter(element) -> checkWhiteSpaceBothBeforeAndAfter(element)
      onlyNeedSpaceBefore(element) -> checkWhiteSpaceBefore(element)
      onlyNeedSpaceAfter(element) -> checkWhiteSpaceAfter(element)
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

  private fun checkWhiteSpaceBothBeforeAndAfter(element: PsiElement) {
    val keyword = element.text
    if (element.nextSibling !is PsiWhiteSpace || element.prevSibling !is PsiWhiteSpace) {
      holder.registerProblem(element, "$keyword 前后应当有空格", ERROR, SpaceQuickFix(SpaceQuickFix.Type.Both))
    }
  }

  private fun needSpaceBothBeforeAndAfter(element: PsiElement): Boolean {
    if (element.language is KotlinLanguage) {
      val test = ((element is LeafPsiElement
          && element.parent !is KtValueArgument
          && element.parent !is KtImportDirective
          && (element.parent !is KtOperationReferenceExpression
          || (element.parent is KtOperationReferenceExpression && element.elementType in listOf(ELVIS)))
          && element.parent !is KtTypeArgumentList
          && element.parent !is KtTypeParameterList
          && ((element.parent !is KtWhenEntry && element.elementType == ELSE_KEYWORD)
          || element.elementType in shouldHaveSpaceBothBeforeAndAfterElementTypes))
          && !(element.elementType == MUL && element.parent is KtTypeProjection))
          || (element is KtOperationReferenceExpression
          && element.firstChild is LeafPsiElement
          && (element.firstChild as LeafPsiElement).elementType in shouldHaveSpaceBothBeforeAndAfterElementTypes)
      return test && notPrefixExpression(element)
    } else {
      return element is PsiJavaToken
          && element.parent !is PsiReferenceParameterList
          && element.parent !is PsiTypeParameterList
          && element.parent !is PsiImportStatement
          && !(element.tokenType == ElementType.MINUS && element.parent is
          PsiPrefixExpression)
          && element.text in shouldHaveSpaceBothBeforeAndAfterTokens
    }
  }

  private fun notPrefixExpression(element: PsiElement): Boolean {
    return !(element is LeafPsiElement && element.elementType == MINUS
        && element.parent is KtOperationReferenceExpression
        && element.parent.parent is KtPrefixExpression
        ) && !(element is KtOperationReferenceExpression && element.parent is KtPrefixExpression)
  }

  private fun onlyNeedSpaceAfter(element: PsiElement): Boolean {
    return (element is LeafPsiElement
        && element.parent !is KtOperationReferenceExpression
        && element.elementType in shouldOnlyHaveSpaceAfterElementTypes) || (element is PsiKeyword
        && element.text in shouldOnlyHaveSpaceAfterKeywords)
  }

  private fun onlyNeedSpaceBefore(element: PsiElement): Boolean {
    return if (element.language is KotlinLanguage) {
      element is PsiJavaToken
          && ((element.tokenType == ElementType.MINUS && element.parent is
          PsiPrefixExpression) || (element.tokenType == CATCH_KEYWORD
          && element.parent is KtCatchClause))
    } else {
      false
    }
  }

  companion object {

    val shouldHaveSpaceBothBeforeAndAfterElementTypes = listOf(
        MUL, PLUS, MINUS, DIV, PERC,
        GT, LT, LTEQ, GTEQ, EQEQEQ, ARROW, DOUBLE_ARROW, EXCLEQEQEQ, EQEQ, EXCLEQ,
        ANDAND, OROR, EQ, MULTEQ, DIVEQ, PERCEQ, PLUSEQ, MINUSEQ, NOT_IN, NOT_IS)
    val shouldHaveSpaceBothBeforeAndAfterKeywords = shouldHaveSpaceBothBeforeAndAfterElementTypes
        .map { it.value } + listOf("else", "catch")
    val shouldHaveSpaceBothBeforeAndAfterTokens = listOf(">", "<", "=", ">=", "<=", "!=", "&&", "||", "&", "|", "==",
        "+", "-", "*", "/", "%", "+=", "-=", "/=", "*=", ">>", "<<", "<>") + shouldHaveSpaceBothBeforeAndAfterKeywords
    val shouldOnlyHaveSpaceAfterElementTypes = listOf(
        CATCH_KEYWORD, FOR_KEYWORD, IF_KEYWORD, TRY_KEYWORD, DO_KEYWORD, WHILE_KEYWORD, WHEN_KEYWORD, ELVIS)
    val shouldOnlyHaveSpaceAfterKeywords = listOf("if", "for", "try",
        "while", "do", "switch")

  }

}

