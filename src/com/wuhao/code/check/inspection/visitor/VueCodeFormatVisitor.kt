/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.LanguageNames
import com.wuhao.code.check.Messages
import com.wuhao.code.check.lang.vue.VueAttrNames.KEY
import com.wuhao.code.check.lang.vue.VueDirectives
import com.wuhao.code.check.lang.vue.VueDirectives.BIND
import com.wuhao.code.check.lang.vue.VueDirectives.FOR
import com.wuhao.code.check.lang.vue.VueDirectives.IF
import com.wuhao.code.check.lang.vue.VueDirectives.ON
import com.wuhao.code.check.registerError
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.SCRIPT_TAG
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.STYLE_TAG
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.TEMPLATE_TAG
import com.wuhao.code.check.style.arrangement.vue.VueRecursiveVisitor
import org.jetbrains.kotlin.idea.refactoring.getLineCount

/**
 *
 * Created by 吴昊 on 18-4-26.
 */
open class VueCodeFormatVisitor(val holder: ProblemsHolder) : VueRecursiveVisitor(), BaseCodeFormatVisitor {

  override fun support(language: Language): Boolean {
    return language.displayName == LanguageNames.vue
  }

  override fun visitXmlAttribute(attribute: XmlAttribute) {
    // v-if和v-for不应出现在同一元素之上
    if (attribute.name == FOR && attribute.parent.getAttribute(IF) != null) {
      holder.registerError(attribute, Messages.ifAndForNotTogether)
    }
    //v-for标签应当有:key属性
    if (attribute.name == FOR && attribute.parent.getAttribute(KEY) == null) {
      holder.registerError(attribute, Messages.forTagShouldHaveKeyAttr, object : LocalQuickFix {

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
          val tag = descriptor.psiElement.parent as XmlTag
          tag.setAttribute(KEY, "")
        }

        override fun getFamilyName(): String {
          return "添加${KEY}属性"
        }

      })
    }
    if (attribute.name == KEY && attribute.value.isNullOrBlank()) {
      holder.registerError(attribute, Messages.missingAttrValue)
    }
    // v-bind和v-on应该缩写
    if (attribute.name.startsWith(BIND) || attribute.name.startsWith(ON)) {
      holder.registerError(attribute, Messages.forShort, object : LocalQuickFix {

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
          val factory = XmlElementFactory.getInstance(project)
          val attr = descriptor.psiElement as XmlAttribute
          if (attr.value != null) {
            val newAttr = factory.createAttribute(VueDirectives.getShortName(attr.name),
                attr.value!!, attr.parent)
            descriptor.psiElement.replace(newAttr)
          }
        }

        override fun getFamilyName(): String {
          return "缩写"
        }
      })
    }
    super.visitXmlAttribute(attribute)
  }

  override fun visitXmlTag(tag: XmlTag) {
    if (tag.parent is XmlDocument) {
      when (tag.name) {
        TEMPLATE_TAG -> {
          if (tag.getLineCount() > MAX_TEMPLATE_LINES) {
            holder.registerProblem(tag, "template长度不得超过${MAX_TEMPLATE_LINES}行")
          }
        }
        SCRIPT_TAG -> {
        }
        STYLE_TAG -> {
        }
      }
    }
  }

  companion object {
    const val MAX_TEMPLATE_LINES = 150
  }

}

