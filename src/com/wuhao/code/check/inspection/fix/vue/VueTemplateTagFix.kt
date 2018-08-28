/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix.vue

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.ancestors
import com.wuhao.code.check.ktPsiFactory

/**
 * vue模板标签属性排序及格式化
 * @author 吴昊
 * @since 1.1
 */
class VueTemplateTagFix(private val sortedAttributes: List<XmlAttribute>) : LocalQuickFix {

  companion object {
    fun comparePrefix(nameList: List<String>, prefix: String): Int {
      return when {
        nameList.all { it.startsWith(prefix) } -> nameList[0].compareTo(nameList[1])
        nameList[0].startsWith(prefix) -> -1
        else -> 1
      }
    }

    fun fixElement(el: XmlTag, sortedAttributes: List<XmlAttribute>) {
      reorderAttributes(el, sortedAttributes)
      fixWhitespace(el)
    }

    fun fixWhitespace(el: XmlTag) {
      val factory = el.ktPsiFactory
      el.attributes.forEachIndexed { index, it ->
        val spaceBefore = it.prevSibling as PsiWhiteSpace
        if (it.value == null && spaceBefore.textContains('\n')) {
          spaceBefore.replace(factory.createWhiteSpace(" "))
        } else if (it.value != null && index != 0 && !spaceBefore.textContains('\n')) {
          spaceBefore.replace(factory.createWhiteSpace("\n"))
        } else if (index == 0 && spaceBefore.textContains('\n')) {
          spaceBefore.replace(factory.createWhiteSpace(" "))
        }
      }
    }

    private fun reorderAttributes(el: XmlTag, sortedAttributes: List<XmlAttribute>) {
      if (el.ancestors.any { it is XmlTag && it.name == "template" && it.parent is XmlDocument }) {
        sortedAttributes.forEach { it.delete() }
        el.children.filter { it is PsiWhiteSpace }
            .forEach { it.delete() }
        sortedAttributes
            .forEach { attr ->
              el.setAttribute(attr.name, attr.value)
            }
      }
    }
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.psiElement as XmlTag
    fixElement(el, sortedAttributes)
  }

  override fun getFamilyName(): String {
    return "属性重新排序"
  }

}

