package com.wuhao.code.check.processors

import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReturnStatement
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionProperty
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPattern
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.wuhao.code.check.PsiPatterns2
import com.wuhao.code.check.endOffset
import com.wuhao.code.check.getAncestor
import com.wuhao.code.check.inspection.fix.JsPropertySortFix
import com.wuhao.code.check.inspection.visitor.TypeScriptCodeFormatVisitor.Companion.isRootVueComponentObject
import com.wuhao.code.check.setBlankLineBefore
import org.jetbrains.vuejs.lang.html.VueLanguage

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
    if (source.language is TypeScriptLanguageDialect
        || source.language is VueLanguage) {
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

  override fun visitJSObjectLiteralExpression(node: JSObjectLiteralExpression) {
    val parent = node.parent
    val possibleVueRootElement = node.getAncestor(2)
    if (possibleVueRootElement is JSObjectLiteralExpression
        && parent is JSProperty
        && parent.name == "props"
        && isRootVueComponentObject(possibleVueRootElement)) {
      JsPropertySortFix.fix(node, false)
    }
    if (PlatformPatterns.psiElement()
        .withParent(JSReturnStatement::class.java)
        .withAncestor(3, PlatformPatterns.psiElement(TypeScriptFunctionProperty::class.java)
            .withName("setup").withParent(JSObjectLiteralExpression::class.java))
        .accepts(node)) {
      JsPropertySortFix.fix(node, false)
    }
    super.visitJSObjectLiteralExpression(node)
  }

}
