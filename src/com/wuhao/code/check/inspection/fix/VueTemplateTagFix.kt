/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.ancestors
import com.wuhao.code.check.inspection.visitor.BaseCodeFormatVisitor
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * vue模板标签属性排序及格式化
 * @author 吴昊
 * @since 1.1
 */
class VueTemplateTagFix(private val sortedAttributes: List<XmlAttribute>) : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.psiElement as XmlTag
    fixElement(el, sortedAttributes)
  }

  override fun getFamilyName(): String {
    return "属性重新排序"
  }

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

    fun fixElement(el: XmlTag) {
      fixElement(el, sortAttributes(el))
    }

    fun fixWhitespace(el: XmlTag) {
      val factory = KtPsiFactory(el.project)
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

    private fun sortAttributes(element: XmlTag): List<XmlAttribute> {
      return element.attributes.sortedWith(Comparator { attr1, attr2 ->
        val name1 = attr1.name
        val name2 = attr2.name
        val nameList = listOf(name1, name2)
        if (attr1.value == null && attr2.value == null) {
          attr1.name.compareTo(attr2.name)
        } else if (attr1.value == null) {
          -1
        } else if (attr2.value == null) {
          1
        } else if (nameList.any { it.startsWith(BaseCodeFormatVisitor.DIRECTIVE_PREFIX) }) {
          comparePrefix(nameList, BaseCodeFormatVisitor.DIRECTIVE_PREFIX)
        } else if (nameList.any {
              !it.startsWith(BaseCodeFormatVisitor.CUSTOM_ATTR_PREFIX) && !it.startsWith(BaseCodeFormatVisitor.ACTION_PREFIX)
            }) {
          if (!name1.startsWith(BaseCodeFormatVisitor.CUSTOM_ATTR_PREFIX) && !name1.startsWith(BaseCodeFormatVisitor.ACTION_PREFIX)
              && !name2.startsWith(BaseCodeFormatVisitor.CUSTOM_ATTR_PREFIX) && !name2.startsWith(BaseCodeFormatVisitor.ACTION_PREFIX)) {
            name1.compareTo(name2)
          } else if (!name1.startsWith(BaseCodeFormatVisitor.CUSTOM_ATTR_PREFIX) && !name1.startsWith(BaseCodeFormatVisitor.ACTION_PREFIX)) {
            -1
          } else {
            1
          }
        } else if (nameList.any { it.startsWith(BaseCodeFormatVisitor.CUSTOM_ATTR_PREFIX) }) {
          comparePrefix(nameList, BaseCodeFormatVisitor.CUSTOM_ATTR_PREFIX)
        } else if (nameList.any { it.startsWith(BaseCodeFormatVisitor.ACTION_PREFIX) }) {
          comparePrefix(nameList, BaseCodeFormatVisitor.ACTION_PREFIX)
        } else {
          0
        }
      }).filter { it.value != null }
    }
  }
}

