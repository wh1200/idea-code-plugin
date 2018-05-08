/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.processors

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.arrangement.engine.ArrangementEngine
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.wuhao.code.check.RecursiveVisitor
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.psiUtil.endOffset

/**
 * 修复kt文件中的空白行
 * @author 吴昊
 * @since 1.1.2
 */
class FixKotlinBlankLineProcessor : PostFormatProcessor {

  val arrangeEngine = ArrangementEngine()

  override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
    return source
  }

  override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
    if (source.language is KotlinLanguage) {
      object : RecursiveVisitor() {
        override fun visitElement(element: PsiElement) {

        }
      }.visit(source)
      arrangeEngine.arrange(source, listOf(rangeToReformat))
    }
    return TextRange(0, source.endOffset)
  }
}

