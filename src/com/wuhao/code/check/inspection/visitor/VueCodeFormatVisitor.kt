/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.constants.LanguageNames
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.depth
import com.wuhao.code.check.inspection.fix.vue.ComplexExpToComputedPropertyFix
import com.wuhao.code.check.inspection.fix.vue.VueComponentNameFix
import com.wuhao.code.check.inspection.fix.vue.VueShortAttrFix
import com.wuhao.code.check.lang.javascript.psi.JSRecursiveElementVisitor
import com.wuhao.code.check.lang.vue.VueAttrNames.KEY
import com.wuhao.code.check.lang.vue.VueDirectives.BIND
import com.wuhao.code.check.lang.vue.VueDirectives.FOR
import com.wuhao.code.check.lang.vue.VueDirectives.IF
import com.wuhao.code.check.lang.vue.VueDirectives.ON
import com.wuhao.code.check.lang.vue.isInjectAttribute
import com.wuhao.code.check.constants.registerError
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.SCRIPT_TAG
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.STYLE_TAG
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.TEMPLATE_TAG
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.vuejs.codeInsight.VueFileVisitor

/**
 *
 * Created by 吴昊 on 18-4-26.
 */
open class VueCodeFormatVisitor(val holder: ProblemsHolder) : VueFileVisitor(), BaseCodeFormatVisitor {

  companion object {
    const val COMPUTED_ATTRIBUTE = "computed"
    const val MAX_TEMPLATE_LINES = 150
  }

  override fun support(language: Language): Boolean {
    return language.displayName == LanguageNames.VUE
  }

  override fun visitXmlAttribute(attribute: XmlAttribute) {
    // v-if和v-for不应出现在同一元素之上
    if (attribute.name == FOR && attribute.parent.getAttribute(IF) != null) {
      holder.registerError(attribute, Messages.IF_AND_FOR_NOT_TOGETHER)
    }
    //v-for标签应当有:key属性
    if (attribute.name == FOR && attribute.parent.getAttribute(KEY) == null) {
      holder.registerError(attribute, Messages.FOR_TAG_SHOULD_HAVE_KEY_ATTR, object : LocalQuickFix {

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
      holder.registerError(attribute, Messages.MISSING_ATTR_VALUE)
    }
    // v-bind和v-on应该缩写
    if (attribute.name.startsWith(BIND) || attribute.name.startsWith(ON)) {
      holder.registerError(attribute, Messages.FOR_SHORT, VueShortAttrFix())
    }
    super.visitXmlAttribute(attribute)
  }

  override fun visitXmlAttributeValue(value: XmlAttributeValue) {
    if (isInjectAttribute(value.parent as XmlAttribute)
        && (value.parent as XmlAttribute).name != FOR
        && !(value.parent as XmlAttribute).name.startsWith(CommonCodeFormatVisitor.ACTION_PREFIX)) {
      val jsContent = value.getChildOfType<JSEmbeddedContent>()
      if (jsContent != null) {
        val depth = jsContent.depth
        if (depth >= 4) {
          holder.registerProblem(jsContent, "复杂的属性应当声明在计算属性中", ProblemHighlightType.INFORMATION,
              ComplexExpToComputedPropertyFix())
        }
      }
    }
    super.visitXmlAttributeValue(value)
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
          val script = tag.getChildOfType<JSEmbeddedContent>()
          if (script != null) {
            script.accept(VueJsVisitor())
          }
        }
        STYLE_TAG -> {
        }
      }
    }
  }

  /**
   * vue文件中js代码递归访问器
   * @author 吴昊
   * @since 1.3.5
   */
  inner class VueJsVisitor : JSRecursiveElementVisitor() {

    override fun visitJSObjectLiteralExpression(node: JSObjectLiteralExpression) {
      if (node.parent is ES6ExportDefaultAssignment) {
        // vue 组件必须有name属性
        if (node.findProperty("name") == null) {
          holder.registerError(node.parent.firstChild, Messages.VUE_COMPONENT_MISSING_NAME, VueComponentNameFix(node))
        }
        super.visitJSObjectLiteralExpression(node)
      }
    }

  }

}

