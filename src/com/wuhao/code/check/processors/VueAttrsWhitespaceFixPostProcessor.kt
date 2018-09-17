/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.processors

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.constants.LanguageNames
import com.wuhao.code.check.endOffset
import com.wuhao.code.check.inspection.fix.vue.VueTemplateTagFix
import com.wuhao.code.check.lang.RecursiveVisitor

/**
 * 格式化代码时自动对vue模板中的标签属性进行排序
 * @author 吴昊
 * @since 1.1
 */
class VueAttrsWhitespaceFixPostProcessor : PostFormatProcessor {

  override fun processElement(el: PsiElement, styleSettings: CodeStyleSettings): PsiElement {
    return el
  }

  override fun processText(file: PsiFile, textRange: TextRange, styleSettings: CodeStyleSettings): TextRange {
    if (file is HtmlFileImpl && file.language.displayName == LanguageNames.VUE) {
      val templateTag = file.document?.children?.firstOrNull { it is XmlTag && it.name == "template" }
      if (templateTag != null) {
        object : RecursiveVisitor() {

          override fun visitElement(element: PsiElement) {
            if (element is XmlTag) {
              VueTemplateTagFix.fixWhitespace(element)
            }
          }

        }.visit(templateTag)
      }
    }
    return TextRange(0, file.endOffset)
  }

}

