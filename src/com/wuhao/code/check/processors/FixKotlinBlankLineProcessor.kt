/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.processors

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.wuhao.code.check.insertElementBefore
import com.wuhao.code.check.style.arrangement.kotlin.KotlinRecursiveVisitor
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.endOffset

/**
 * 修复kt文件中的空白行
 * @author 吴昊
 * @since 1.1.2
 */
class FixKotlinBlankLineProcessor : PostFormatProcessor {

  override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
    return source
  }

  override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
    if (source.language is KotlinLanguage) {
      val factory = KtPsiFactory(source.project)
      source.accept(object : KotlinRecursiveVisitor() {
        override fun visitClassBody(classBody: KtClassBody, data: Any?) {
          val lBrace = classBody.lBrace
          val rBrace = classBody.rBrace
          if (lBrace != null && rBrace != null) {
            if (rBrace.prevSibling !is PsiWhiteSpace) {
              rBrace.insertElementBefore(factory.createNewLine(2))
            } else {
              if (rBrace.prevSibling === lBrace.nextSibling && rBrace.prevSibling.getLineCount() != 1) {
                rBrace.prevSibling.replace(factory.createNewLine(1))
              } else if (classBody.rBrace!!.prevSibling.getLineCount() != 2) {
                rBrace.prevSibling.replace(factory.createNewLine(2))
              } else if (classBody.lBrace!!.nextSibling.getLineCount() != 2) {
                lBrace.nextSibling.replace(factory.createNewLine(2))
              }
            }
          }
        }
      })
    }
    return TextRange(0, source.endOffset)
  }
}

