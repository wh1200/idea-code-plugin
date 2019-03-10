package com.wuhao.code.check.processors

import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.wuhao.code.check.endOffset
import com.wuhao.code.check.setBlankLineBefore

/**
 *
 * Created by 吴昊 on 2019-03-10.
 *
 * @author 吴昊
 * @since
 */
class FixTypeScriptPostProcessor : PostFormatProcessor {

  override fun processElement(source: PsiElement, p1: CodeStyleSettings): PsiElement {
    return source
  }

  override fun processText(source: PsiFile, range: TextRange, styleSettings: CodeStyleSettings): TextRange {
    if (source.language is TypeScriptLanguageDialect) {
      source.accept(TypeScriptFixVisitor())
    }
    return TextRange(0, source.endOffset)
  }

}

class TypeScriptFixVisitor() : JSElementVisitor() {

  override fun visitElement(element: PsiElement) {
    if (element is TypeScriptPropertySignature) {
      element.setBlankLineBefore(0)
    }
    element.acceptChildren(this)
  }

}
