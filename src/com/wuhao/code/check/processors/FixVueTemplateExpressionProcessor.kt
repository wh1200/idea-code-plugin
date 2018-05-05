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
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.RecursiveVisitor
import com.wuhao.code.check.insertAfter
import com.wuhao.code.check.insertBefore
import com.wuhao.code.check.inspection.CodeFormatVisitor
import com.wuhao.code.check.inspection.visitor.VueCodeFormatVisitor.Companion.CUSTOM_ATTR_PREFIX
import com.wuhao.code.check.inspection.visitor.VueCodeFormatVisitor.Companion.DIRECTIVE_PREFIX
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.endOffset

class FixVueTemplateExpressionProcessor : PostFormatProcessor {

  override fun processElement(el: PsiElement, styleSettings: CodeStyleSettings): PsiElement {
    return el
  }

  override fun processText(file: PsiFile, textRange: TextRange, styleSettings: CodeStyleSettings): TextRange {
    if (file is HtmlFileImpl && file.language.displayName == CodeFormatVisitor.VUE_LANGUAGE) {
      val templateTag = file.document?.children?.firstOrNull { it is XmlTag && it.name == "template" }
      if (templateTag != null) {
        processElements(arrayOf(templateTag))
      }
    }
    return TextRange(0, file.endOffset)
  }

  private fun processElements(children: Array<out PsiElement>) {
    children.forEach {
      val parent = it.parent
      if (it.text != "\"" && it.text != "'" && parent is XmlAttributeValue) {
        val attr = parent.parent
        if (attr is XmlAttribute
            && (attr.name.startsWith(CUSTOM_ATTR_PREFIX)
                || attr.name.startsWith(DIRECTIVE_PREFIX))) {
          val exp = JSElementFactory.createExpressionCodeFragment(it.project, it.text, null)
          object : RecursiveVisitor(exp) {
            private val factory = KtPsiFactory(exp.project)
            override fun visit(element: PsiElement) {
              if (element.text in listOf("+", "-", "*", "/", "?", ":", ">", "<", "=", "!=", "===", "==", ">=", "<=", "||", "%",
                      "&&", "&", "|")) {
                if (element.prevSibling !is PsiWhiteSpace) {
                  element.insertBefore(factory.createWhiteSpace(" "))
                }
                if (element.nextSibling !is PsiWhiteSpace) {
                  element.insertAfter(factory.createWhiteSpace(" "))
                }
              }
              if (element.text in listOf(",")) {
                if (element.nextSibling !is PsiWhiteSpace) {
                  element.insertAfter(factory.createWhiteSpace(" "))
                }
              }
            }
          }.run()
          attr.value = exp.text
        }
      }

      if (it.children.isNotEmpty()) {
        processElements(it.children)
      }
    }
  }
}

