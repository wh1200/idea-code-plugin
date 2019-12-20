/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.psi.JSBinaryExpression
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionSignature
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil.SCRIPT_TAG_NAME
import com.wuhao.code.check.constants.LanguageNames
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.constants.registerError
import com.wuhao.code.check.constants.registerWarning
import com.wuhao.code.check.depth
import com.wuhao.code.check.getChildByType
import com.wuhao.code.check.getLineCount
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
import org.jetbrains.kotlin.idea.core.replaced
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.vuejs.index.VueFileVisitor
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpression

/**
 *
 * Created by 吴昊 on 18-4-26.
 */
open class VueCodeFormatVisitor(val holder: ProblemsHolder) : VueFileVisitor(), BaseCodeFormatVisitor {

  companion object {
    const val COMPUTED_ATTRIBUTE = "computed"
    const val MAX_TEMPLATE_LINES = 300
    const val SLOT_TAG = "slot"
    const val TEMPLATE_TAG = "template"
  }

  override fun support(language: Language): Boolean {
    return language.displayName == LanguageNames.VUE
  }

  override fun visitXmlAttribute(attribute: XmlAttribute) {
    // v-if和v-for不应出现在同一元素之上
    if (attribute.name == FOR && attribute.parent.getAttribute(IF) != null) {
      holder.registerWarning(attribute, Messages.IF_AND_FOR_NOT_TOGETHER, object : LocalQuickFix {

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
          val attr = descriptor.psiElement as XmlAttribute
          val value = attr.value
          val originTag = (descriptor.psiElement as XmlAttribute).parent
          attr.delete()
          val newTag = XmlElementFactory.getInstance(project)
              .createTagFromText(
                  """
            <template v-for="$value">
              ${originTag.text}
            </template>
          """.trimIndent()
              )
          originTag.replaced(newTag)
        }

        override fun getFamilyName(): String {
          return "将v-for属性提取到template"
        }

      })
    }
    //v-for标签应当有:key属性
    if (attribute.name == FOR && attribute.parent.name != SLOT_TAG) {
      if ((attribute.parent.name == TEMPLATE_TAG
              && keyAttrOnChild(attribute.parent) == null)
          || (attribute.parent.name != TEMPLATE_TAG && attribute.parent.getAttribute(KEY) == null)
      ) {
        holder.registerWarning(attribute, Messages.FOR_TAG_SHOULD_HAVE_KEY_ATTR, object : LocalQuickFix {

          override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val tag = descriptor.psiElement.parent as XmlTag
            if (tag.name == TEMPLATE_TAG) {
              val child = tag.getChildByType<XmlTag>()
              if (child != null) {
                child.setAttribute(KEY, "")
              } else {
                val div = XmlElementFactory.getInstance(project).createTagFromText("<div :key=''></div>")
                tag.add(div)
              }
            } else {
              tag.setAttribute(KEY, "")
            }
          }

          override fun getFamilyName(): String {
            return "添加${KEY}属性"
          }

        })
      }
    }
    if (attribute.name == KEY && attribute.value.isNullOrBlank()) {
      holder.registerWarning(attribute, Messages.MISSING_ATTR_VALUE)
    }
    // v-bind和v-on应该缩写
    if (attribute.name.startsWith(BIND) || attribute.name.startsWith(ON)) {
      holder.registerWarning(attribute, Messages.FOR_SHORT, VueShortAttrFix())
    }
    super.visitXmlAttribute(attribute)
  }

  override fun visitXmlAttributeValue(value: XmlAttributeValue) {
    if (isInjectAttribute(value.parent as XmlAttribute)
        && (value.parent as XmlAttribute).name != FOR
        && !(value.parent as XmlAttribute).name.startsWith(CommonCodeFormatVisitor.ACTION_PREFIX)
    ) {
      val astWrapper = value.getChildByType<ASTWrapperPsiElement>()
      if (astWrapper != null) {
        val vueJSEmbeddedExpression = astWrapper.getChildOfType<VueJSEmbeddedExpression>()
        if (vueJSEmbeddedExpression != null && vueJSEmbeddedExpression.getChildOfType<JSBinaryExpression>() != null) {
          val exp = vueJSEmbeddedExpression.getChildOfType<JSBinaryExpression>()!!
          if (exp.depth >= 4) {
            holder.registerProblem(
                exp, "复杂的属性应当声明在计算属性中",
                ProblemHighlightType.INFORMATION,
                ComplexExpToComputedPropertyFix()
            )
          }
        }
      } else {
        val jsContent = value.getChildByType<JSEmbeddedContent>()
        if (jsContent != null) {
          val depth = jsContent.depth
          if (depth >= 4) {
            holder.registerProblem(
                jsContent, "复杂的属性应当声明在计算属性中",
                ProblemHighlightType.INFORMATION,
                ComplexExpToComputedPropertyFix()
            )
          }
        }
      }
    }
    super.visitXmlAttributeValue(value)
  }

  override fun visitXmlTag(tag: XmlTag) {
    if (tag.parent is XmlDocument) {
      when (tag.name) {
        TEMPLATE_TAG    -> {
          if (tag.getLineCount() > MAX_TEMPLATE_LINES) {
            holder.registerProblem(tag, "template长度不得超过${MAX_TEMPLATE_LINES}行")
          }
        }
        SCRIPT_TAG_NAME -> {
          tag.getChildByType<JSEmbeddedContent>()?.accept(VueJsVisitor())
        }
      }
    }
    super.visitXmlTag(tag)
  }

  private fun keyAttrOnChild(parent: XmlTag): XmlAttribute? {
    var son = parent.getChildByType<XmlTag>()
    while (son?.name == TEMPLATE_TAG) {
      son = son.getChildByType()
    }
    return son?.getAttribute(KEY)
  }

  /**
   * vue文件中js代码递归访问器
   * @author 吴昊
   * @since 1.3.5
   */
  inner class VueJsVisitor : JSRecursiveElementVisitor() {

    override fun visitElement(element: PsiElement) {
      if (element is TypeScriptField) {
        visitTypeScriptField(element)
      }
      super.visitElement(element)
    }

    override fun visitJSObjectLiteralExpression(node: JSObjectLiteralExpression) {
      if (node.parent is ES6ExportDefaultAssignment) {
        // vue 组件必须有name属性
        if (node.findProperty("name") == null) {
          holder.registerWarning(node.parent.firstChild, Messages.VUE_COMPONENT_MISSING_NAME, VueComponentNameFix(node))
        }
        super.visitJSObjectLiteralExpression(node)
      }
    }

    override fun visitJSReferenceExpression(node: JSReferenceExpression) {
      if (listOf("axios.get", "axios.post", "axios.put", "axios.delete").contains(node.text)) {
        holder.registerWarning(node, "避免直接使用axios调用接口")
      }
      super.visitJSReferenceExpression(node)
    }

    override fun visitTypeScriptFunctionSignature(functionSignature: TypeScriptFunctionSignature) {
      functionSignature.parameters.forEach {
        if (it.typeElement == null) {
          holder.registerError(it, Messages.PARAMETER_MISSING_TYPE)
        }
      }
      super.visitTypeScriptFunctionSignature(functionSignature)
    }

    private fun visitTypeScriptField(element: TypeScriptField) {
      if (element.typeElement == null && element.nameIdentifier != null) {
        holder.registerError(element.nameIdentifier!!, "缺少类型")
      }
    }

  }

}
