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
import com.wuhao.code.check.LanguageNames
import com.wuhao.code.check.RecursiveVisitor
import com.wuhao.code.check.insertElementAfter
import com.wuhao.code.check.insertElementBefore
import com.wuhao.code.check.inspection.visitor.CommonCodeFormatVisitor.Companion.CUSTOM_ATTR_PREFIX
import com.wuhao.code.check.inspection.visitor.CommonCodeFormatVisitor.Companion.DIRECTIVE_PREFIX
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.endOffset

/**
 * 格式化代码时自动修复模板中标签属性的换行
 * @author
 * @since
 */
class FixVueTemplateExpressionProcessor : PostFormatProcessor {

  override fun processElement(el: PsiElement, styleSettings: CodeStyleSettings): PsiElement {
    return el
  }

  override fun processText(file: PsiFile, textRange: TextRange, styleSettings: CodeStyleSettings): TextRange {
    if (file is HtmlFileImpl && file.language.displayName == LanguageNames.vue) {
      val templateTag = file.document?.children?.firstOrNull { it is XmlTag && it.name == "template" }
      if (templateTag != null) {
        object:RecursiveVisitor(){
          override fun visitElement(element: PsiElement) {
            val parent = element.parent
            if (element.text != "\"" && element.text != "'" && parent is XmlAttributeValue) {
              val attr = parent.parent
              if (attr is XmlAttribute
                  && (attr.name.startsWith(CUSTOM_ATTR_PREFIX)
                      || attr.name.startsWith(DIRECTIVE_PREFIX))) {
                val exp = JSElementFactory.createExpressionCodeFragment(element.project, element.text, null)
                object : RecursiveVisitor() {
                  private val factory = KtPsiFactory(exp.project)
                  override fun visitElement(element: PsiElement) {
                    if (element.text in listOf("+", "-", "*", "/", "?", ":", ">", "<", "=", "!=", "===", "==", ">=", "<=", "||", "%",
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
                attr.value = exp.text
              }
            }
          }
        }.visit(templateTag)
      }
    }
    return TextRange(0, file.endOffset)
  }

}

