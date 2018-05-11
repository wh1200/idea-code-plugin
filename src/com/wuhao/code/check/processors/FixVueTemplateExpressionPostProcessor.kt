/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.processors

import com.intellij.lang.javascript.psi.JSElementFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.LanguageNames
import com.wuhao.code.check.insertElementAfter
import com.wuhao.code.check.insertElementBefore
import com.wuhao.code.check.lang.RecursiveVisitor
import com.wuhao.code.check.lang.vue.isInjectAttribute
import com.wuhao.code.check.style.arrangement.vue.VueRecursiveVisitor
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.endOffset

/**
 * 格式化代码时自动修复模板中标签属性的换行
 * @author
 * @since
 */
class FixVueTemplateExpressionPostProcessor : PostFormatProcessor {

  override fun processElement(el: PsiElement, styleSettings: CodeStyleSettings): PsiElement {
    return el
  }

  override fun processText(file: PsiFile, textRange: TextRange, styleSettings: CodeStyleSettings): TextRange {
    if (file is HtmlFileImpl && file.language.displayName == LanguageNames.vue) {
      val templateTag = file.document?.children?.firstOrNull { it is XmlTag && it.name == "template" }
      if (templateTag != null) {
        templateTag.accept(object : VueRecursiveVisitor() {

          override fun visitXmlAttribute(attribute: XmlAttribute) {
            if (isInjectAttribute(attribute)) {
              val exp = JSElementFactory.createExpressionCodeFragment(attribute.project, attribute.value, attribute)
              object : RecursiveVisitor() {
                private val factory = KtPsiFactory(exp.project)
                override fun visitElement(element: PsiElement) {
                  if (element.text in listOf(",", "+", "-", "*", "/", "?",
                          ":", ">", "<", "=", "!=", "===", "==", "===",
                          ">=", "<=", "||", "%",
                          "&&", "&", "|")) {
                    if (element.prevSibling !is PsiWhiteSpace) {
                      element.insertElementBefore(factory.createWhiteSpace(" "))
                    }
                    if (element.nextSibling !is PsiWhiteSpace) {
                      element.insertElementAfter(factory.createWhiteSpace(" "))
                    }
                  }
                  if (element.text in listOf(",")) {
                    if (element.nextSibling !is PsiWhiteSpace) {
                      element.insertElementAfter(factory.createWhiteSpace(" "))
                    }
                  }
                }
              }.visit(exp)
              attribute.value = exp.text
            }
            super.visitXmlAttribute(attribute)
          }
        })
      }
    }
    return TextRange(0, file.endOffset)
  }

}

