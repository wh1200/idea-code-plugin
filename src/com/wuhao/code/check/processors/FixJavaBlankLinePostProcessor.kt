/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.processors

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.javadoc.PsiDocComment
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
class FixJavaBlankLinePostProcessor : PostFormatProcessor {

  override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
    return source
  }

  override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
    val factory = KtPsiFactory(source)
    source.accept(object : JavaRecursiveElementVisitor() {

      override fun visitClass(aClass: PsiClass) {
        setBoth2(aClass)
      }

      override fun visitDocComment(comment: PsiDocComment) {
        fixBlankLineAfter(comment, 1, factory)
      }

      override fun visitElement(element: PsiElement) {
        if (element.parent is PsiClass && element is PsiJavaToken && element.text == "{") {
          fixBlankLineAfter(element, 2, factory)
        }
      }

      override fun visitField(field: PsiField) {
        val nextField = field.getNextSiblingIgnoringWhitespace()
        val prevField = field.getPrevSiblingIgnoringWhitespaceAndComments()
        if (prevField !is PsiField) {
          fixBlankLineBefore(field, 2, factory)
        }
        if (nextField is PsiField) {
          fixBlankLineAfter(field, 1, factory)
        } else {
          fixBlankLineAfter(field, 2, factory)
        }
      }

      override fun visitImportList(list: PsiImportList) {
        setBoth2(list)
      }

      override fun visitJavaFile(file: PsiJavaFile) {
        if (file.firstChild is PsiWhiteSpace) {
          file.firstChild.delete()
        }
      }

      override fun visitMethod(method: PsiMethod) {
        val nextMethod = method.getNextSiblingIgnoringWhitespace()
        val prevMethod = method.getPrevSiblingIgnoringWhitespaceAndComments()
        if (prevMethod !is PsiMethod) {
          fixBlankLineBefore(method, 2, factory)
        }
        if (nextMethod is PsiMethod) {
          if (method.modifiers.contentEquals(nextMethod.modifiers)) {
            fixBlankLineAfter(method, 2, factory)
          } else {
            fixBlankLineAfter(method, 3, factory)
          }
        } else {
          fixBlankLineAfter(method, 3, factory)
        }
      }

      override fun visitPackageStatement(statement: PsiPackageStatement) {
        fixBlankLineBefore(statement, 1, factory)
        fixBlankLineAfter(statement, 2, factory)
      }

      private fun setBoth2(element: PsiElement) {
        fixBlankLineBefore(element, 2, factory)
        fixBlankLineAfter(element, 2, factory)
      }
    })
    fixWhiteSpace(source.lastChild, 2, factory)
    return TextRange(0, source.endOffset)
  }

  private fun fixBlankLineAfter(element: PsiElement, blankLineCount: Int, factory: KtPsiFactory) {
    val whiteSpaceElement = element.nextSibling
    fixWhiteSpace(whiteSpaceElement, blankLineCount, factory)
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

}

