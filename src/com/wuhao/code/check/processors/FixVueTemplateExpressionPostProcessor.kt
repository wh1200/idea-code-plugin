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
import com.wuhao.code.check.constants.LanguageNames
import com.wuhao.code.check.createJsWhiteSpace
import com.wuhao.code.check.endOffset
import com.wuhao.code.check.insertElementAfter
import com.wuhao.code.check.insertElementBefore
import com.wuhao.code.check.lang.RecursiveVisitor
import com.wuhao.code.check.lang.vue.isInjectAttribute
import com.wuhao.code.check.style.arrangement.vue.VueRecursiveVisitor

/**
 * 格式化代码时自动修复模板中标签属性的换行
 * @author wuhao
 * @since 1.1
 */
class FixVueTemplateExpressionPostProcessor : PostFormatProcessor {

  override fun processElement(el: PsiElement, styleSettings: CodeStyleSettings): PsiElement {
    return el
  }

  override fun processText(file: PsiFile, textRange: TextRange, styleSettings: CodeStyleSettings): TextRange {
    if (file is HtmlFileImpl && file.language.displayName == LanguageNames.VUE) {
      val templateTag = file.document?.children?.firstOrNull { it is XmlTag && it.name == "template" }
      templateTag?.accept(object : VueRecursiveVisitor() {

        override fun visitXmlAttribute(attribute: XmlAttribute) {
          if (isInjectAttribute(attribute) && attribute.value != null) {
            val exp = JSElementFactory.createExpressionCodeFragment(attribute.project, attribute.value, attribute)
            JSExpressionVisitor().visit(exp)
            attribute.setValue(exp.text)
          }
          super.visitXmlAttribute(attribute)
        }

      })
    }
    return TextRange(0, file.endOffset)
  }

}

/**
 * js表达式访问器
 * @author 吴昊
 * @since 1.1
 */
class JSExpressionVisitor : RecursiveVisitor() {

  override fun visitElement(element: PsiElement) {
    if (element.text in listOf(",", "+", "-", "*", "/", "?",
            ":", ">", "<", "=", "!=", "===", "==", "===",
            ">=", "<=", "||", "%",
            "&&", "&", "|")) {
      if (element.text == ",") {
        if (element.nextSibling !is PsiWhiteSpace) {
          element.insertElementAfter(element.createJsWhiteSpace(" "))
        }
      } else {
        if (element.prevSibling !is PsiWhiteSpace) {
          element.insertElementBefore(element.createJsWhiteSpace(" "))
        }
        if (element.nextSibling !is PsiWhiteSpace) {
          element.insertElementAfter(element.createJsWhiteSpace(" "))
        }
      }

    }
    if (element.text in listOf(",")) {
      if (element.nextSibling !is PsiWhiteSpace) {
        element.insertElementAfter(element.createJsWhiteSpace(" "))
      }
    }
  }

}

