/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.processors

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.javadoc.PsiDocComment
import com.wuhao.code.check.RecursiveVisitor
import com.wuhao.code.check.insertElementAfter
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.getNextSiblingIgnoringWhitespace
import org.jetbrains.kotlin.psi.psiUtil.getPrevSiblingIgnoringWhitespaceAndComments

/**
 * 修复java文件中的空白行
 * @author 吴昊
 * @since 1.1.2
 */
class FixJavaBlankLineProcessor : PostFormatProcessor {

  override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
    return source
  }

  override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
    val factory = KtPsiFactory(source)
    object : RecursiveVisitor() {
      override fun visitElement(element: PsiElement) {
        if (element is PsiFile && element.firstChild is PsiWhiteSpace) {
          element.firstChild.delete()
        }
        if (element is PsiPackageStatement) {
          fixBlankLineBefore(element, 1, factory)
          fixBlankLineAfter(element, 2, factory)
        }
        if (element is PsiImportList || element is PsiClass) {
          fixBlankLineBefore(element, 2, factory)
          fixBlankLineAfter(element, 2, factory)
        }
        if (element is PsiDocComment) {
          fixBlankLineAfter(element, 1, factory)
        }
        if (element.parent is PsiClass && element is PsiJavaToken && element.text == "{") {
          fixBlankLineAfter(element, 2, factory)
        }
        if (element is PsiField) {
          val nextField = element.getNextSiblingIgnoringWhitespace()
          val prevField = element.getPrevSiblingIgnoringWhitespaceAndComments()
          if(prevField !is PsiField) {
            fixBlankLineBefore(element, 2, factory)
          }
          if (nextField is PsiField) {
            fixBlankLineAfter(element, 1, factory)
//            if (element.modifiers.contentEquals(nextField.modifiers)) {
//            } else {
//            }
          } else {
            fixBlankLineAfter(element, 2, factory)
          }
        }
        if (element is PsiMethod) {
          val nextMethod = element.getNextSiblingIgnoringWhitespace()
          val prevMethod = element.getPrevSiblingIgnoringWhitespaceAndComments()
          if (prevMethod !is PsiMethod) {
            fixBlankLineBefore(element, 2, factory)
          }
          if (nextMethod is PsiMethod) {
            if (element.modifiers.contentEquals(nextMethod.modifiers)) {
              fixBlankLineAfter(element, 2, factory)
            } else {
              fixBlankLineAfter(element, 3, factory)
            }
          } else {
            fixBlankLineAfter(element, 3, factory)
          }
        }
      }
    }.visit(source)
    fixWhiteSpace(source.lastChild, 2, factory)
    return TextRange(0, source.endOffset)
  }

  private fun fixBlankLineBefore(element: PsiElement, blankLineCount: Int, factory: KtPsiFactory) {
    val whiteSpaceElement = element.prevSibling
    fixWhiteSpace(whiteSpaceElement, blankLineCount, factory)
  }

  private fun fixWhiteSpace(whiteSpaceElement: PsiElement?, blankLineCount: Int, factory: KtPsiFactory) {
    if (whiteSpaceElement != null) {
      if (whiteSpaceElement is PsiWhiteSpace) {
        whiteSpaceElement.replace(factory.createWhiteSpace("\n".repeat(blankLineCount)))
      } else {
        whiteSpaceElement.insertElementAfter(factory.createWhiteSpace("\n".repeat(blankLineCount)))
      }
    }
  }

  private fun fixBlankLineAfter(element: PsiElement, blankLineCount: Int, factory: KtPsiFactory) {
    val whiteSpaceElement = element.nextSibling
    fixWhiteSpace(whiteSpaceElement, blankLineCount, factory)
  }
}
