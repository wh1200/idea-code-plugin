/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*
import com.intellij.psi.PsiKeyword.*
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.constants.registerWarning
import com.wuhao.code.check.inspection.fix.SpaceQuickFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Position.After
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Position.Before
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Position.Both
import com.wuhao.code.check.isIdea
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.*
import org.jetbrains.kotlin.psi.*

/**
 * java和kotlin空格检查
 * @author 吴昊
 * @since 1.3.3
 */
class SpaceChecker {

  companion object {
    val SHOULD_HAVE_SPACE_BOTH_BEFORE_AND_AFTER_TOKENS = listOf(">", "<", "=", ">=", "<=", "!=",
        "&&", "||", "&", "|", "==", "+", "-", "*", "/", "%",
        "+=", "-=", "/=", "*=", ">>", "<<", "<>", ELSE, FINALLY)
    val SHOULD_NOT_CHECK_ONLY_ONE_SPACE_AFTER = listOf("+", "->", "=")
    val SHOULD_NOT_CHECK_ONLY_ONE_SPACE_BEFORE = listOf(
        "&&", "||", "+", "->", "=")
  }

  fun checkSpace(element: PsiElement, holder: ProblemsHolder) {
    when {
      needSpaceBothBeforeAndAfter(element) -> checkWhiteSpaceBothBeforeAndAfter(element, holder)
      onlyNeedSpaceBefore(element)         -> checkWhiteSpaceBefore(element, holder)
      onlyNeedSpaceAfter(element)          -> checkWhiteSpaceAfter(element, holder)
    }
  }

  private fun checkOnlyOneSpaceBeforeOrAfter(element: PsiElement,
                                             holder: ProblemsHolder,
                                             position: SpaceQuickFix.Position) {
    if (position == Both) {
      checkOnlyOneSpaceBeforeOrAfter(element, holder, Before)
      checkOnlyOneSpaceBeforeOrAfter(element, holder, After)
    } else {
      if ((shouldCheckOnlyOneSpaceBefore(element) || position != Before)
          && (shouldCheckOnlyOneSpaceAfter(element) || position != After)) {
        val actionElement = when (position) {
          Before -> element.prevSibling
          else   -> element.nextSibling
        }
        if (actionElement is PsiWhiteSpace && actionElement.textLength != 1) {
          val positionDescription = when (position) {
            Before -> "前面"
            else   -> "后面"
          }
          holder.registerWarning(element, "${element.text} ${positionDescription}应当只有1个空格",
              SpaceQuickFix(SpaceQuickFix.Position.After))
        }
      }
    }
  }

  private fun checkWhiteSpaceAfter(element: PsiElement, holder: ProblemsHolder) {
    val keyword = element.text
    if (element.nextSibling !is PsiWhiteSpace) {
      holder.registerWarning(element, "$keyword 之后应当有空格", SpaceQuickFix(After))
    } else {
      checkOnlyOneSpaceBeforeOrAfter(element, holder, After)
    }
  }

  private fun checkWhiteSpaceBefore(element: PsiElement, holder: ProblemsHolder) {
    val keyword = if (element is PsiCatchSection) {
      CATCH
    } else {
      element.text
    }
    if (element.prevSibling !is PsiWhiteSpace) {
      val actionElement = if (element is PsiCatchSection) {
        element.firstChild
      } else {
        element
      }
      holder.registerWarning(actionElement, "$keyword 之前应当有空格",
          SpaceQuickFix(SpaceQuickFix.Position.BeforeParent))
    } else {
      checkOnlyOneSpaceBeforeOrAfter(element, holder, Before)
    }
  }

  private fun checkWhiteSpaceBothBeforeAndAfter(element: PsiElement, holder: ProblemsHolder) {
    val keyword = element.text
    if (element.nextSibling !is PsiWhiteSpace || element.prevSibling !is PsiWhiteSpace) {
      holder.registerWarning(element, "$keyword 前后应当有空格", SpaceQuickFix(Both))
    } else {
      checkOnlyOneSpaceBeforeOrAfter(element, holder, Both)
    }
  }

  private fun needSpaceBothBeforeAndAfter(element: PsiElement): Boolean {
    if (isIdea && element.language is KotlinLanguage) {
      val shouldHaveSpaceBothBeforeAndAfterElementTypes = listOf(
          MUL, PLUS, MINUS, DIV, PERC, GT, LT, LTEQ, GTEQ, EQEQEQ,
          ARROW, DOUBLE_ARROW, EXCLEQEQEQ, EQEQ, EXCLEQ,
          ANDAND, OROR, EQ, MULTEQ, DIVEQ, PERCEQ, PLUSEQ,
          MINUSEQ, NOT_IN, NOT_IS)
      val test: Boolean = ((element is LeafPsiElement
          && element.parent !is KtValueArgument
          && element.parent !is KtImportDirective
          && (element.parent !is KtOperationReferenceExpression
          || (element.parent is KtOperationReferenceExpression && element.elementType in listOf(ELVIS)))
          && element.parent !is KtTypeArgumentList
          && element.parent !is KtTypeParameterList
          && ((element.parent !is KtWhenEntry && element.elementType == KtTokens.ELSE_KEYWORD)
          || element.elementType in shouldHaveSpaceBothBeforeAndAfterElementTypes))
          && !(element.elementType == KtTokens.MUL && element.parent is KtTypeProjection))
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
          && element.text in SHOULD_HAVE_SPACE_BOTH_BEFORE_AND_AFTER_TOKENS
    }
  }

  private fun notPrefixExpression(element: PsiElement): Boolean {
    return !(element is LeafPsiElement && element.elementType == MINUS
        && element.parent is KtOperationReferenceExpression
        && element.parent.parent is KtPrefixExpression
        ) && !(element is KtOperationReferenceExpression && element.parent is KtPrefixExpression)
  }

  private fun onlyNeedSpaceAfter(element: PsiElement): Boolean {
    if (isIdea) {
      val shouldOnlyHaveSpaceAfterElementTypes = listOf(
          CATCH_KEYWORD, FOR_KEYWORD, IF_KEYWORD, TRY_KEYWORD, DO_KEYWORD,
          WHILE_KEYWORD, WHEN_KEYWORD, ELVIS)
      val shouldOnlyHaveSpaceAfterKeywords = listOf(IF, FOR, TRY,
          WHILE, DO, SWITCH, CATCH, CASE, SYNCHRONIZED)
      return (element is LeafPsiElement
          && element.parent !is KtOperationReferenceExpression
          && element.elementType in shouldOnlyHaveSpaceAfterElementTypes) || (element is PsiKeyword
          && element.text in shouldOnlyHaveSpaceAfterKeywords)
    }
    return false
  }

  private fun onlyNeedSpaceBefore(element: PsiElement): Boolean {
    return if (isIdea && element.language is KotlinLanguage) {
      element is PsiJavaToken
          && ((element.tokenType == ElementType.MINUS && element.parent is
          PsiPrefixExpression) || (element.tokenType == CATCH_KEYWORD
          && element.parent is KtCatchClause))
    } else {
      element is PsiCatchSection
    }
  }

  private fun shouldCheckOnlyOneSpaceAfter(element: PsiElement): Boolean {
    return element.text !in SHOULD_NOT_CHECK_ONLY_ONE_SPACE_AFTER
  }

  private fun shouldCheckOnlyOneSpaceBefore(element: PsiElement): Boolean {
    return element.text !in SHOULD_NOT_CHECK_ONLY_ONE_SPACE_BEFORE
  }

}

