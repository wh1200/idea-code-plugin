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
import com.wuhao.code.check.inspection.CodeFormatVisitor.Companion.VUE_LANGUAGE
import com.wuhao.code.check.inspection.fix.VueTemplateTagFix
import org.jetbrains.kotlin.psi.psiUtil.endOffset

/**
 * vue文件中的模板标签属性排序处理
 * @author 吴昊
 * @since 1.1
 */
class ReorderVueAttributesProcessor : PostFormatProcessor {

  override fun processElement(el: PsiElement, styleSettings: CodeStyleSettings): PsiElement {
    if (el.language.displayName == VUE_LANGUAGE && el is XmlTag) {
      VueTemplateTagFix.fixElement(el)
    }
    return el
  }

  override fun processText(file: PsiFile, textRange: TextRange, styleSettings: CodeStyleSettings): TextRange {
    if (file is HtmlFileImpl && file.language.displayName == VUE_LANGUAGE) {
      val templateTag = file.document?.children?.firstOrNull { it is XmlTag && it.name == "template" }
      if (templateTag != null) {
        processElements(arrayOf(templateTag))
      }
    }
    return TextRange(0, file.endOffset)
  }

  private fun processElements(children: Array<out PsiElement>) {
    children.forEach {
      if (it is XmlTag) {
        VueTemplateTagFix.reorderAttributes(it)
      }
      if (it.children.isNotEmpty()) {
        processElements(it.children)
      }
    }
  }

}
