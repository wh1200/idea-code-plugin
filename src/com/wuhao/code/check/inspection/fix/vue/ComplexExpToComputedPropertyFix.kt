package com.wuhao.code.check.inspection.fix.vue

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.*
import com.wuhao.code.check.inspection.visitor.VueCodeFormatVisitor.Companion.COMPUTED_ATTRIBUTE
import com.wuhao.code.check.lang.vue.VueDirectives.FOR
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.vuejs.language.VueVForExpression

/**
 * vue组件模板中，引用的复杂js表达式导出为计算属性的修复
 * @author 吴昊
 * @since 1.3.5
 */
class ComplexExpToComputedPropertyFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.psiElement
    val forVariables = el.getAncestorsOfType<XmlTag>()
        .filter { it.getAttribute(FOR) != null }
        .map {
          it.getAttribute(FOR)?.valueElement?.getChildOfType<JSEmbeddedContent>()?.firstChild as
              VueVForExpression?
        }.map { it?.getVarStatement()?.variables?.toList() }.filterNotNull()
        .flatten().map { it.name }.filterNotNull()
    val arguments = sortedSetOf<String>()
    object : MyRecursiveElementVisitor() {

      override fun visitElement(element: PsiElement) {
        when (element) {
          is JSReferenceExpression -> {
            if (element.firstChild !is JSThisExpression) {
              if (element.firstChild.text !in forVariables) {
                val exp = JSChangeUtil.createExpressionWithContext("this.${element.text}", element.parent)!!.psi
                element.replace(exp)
              } else {
                arguments.add(element.firstChild.text)
              }
            }
          }
        }
      }

    }.visit(el)
    val script = el.parent.ancestorOfType<XmlDocument>()!!
        .firstChild { it is XmlTag && it.name == VueArrangementVisitor.SCRIPT_TAG }
    if (script != null) {
      val obj = script.firstChild { it is JSEmbeddedContent }!!
          .firstChild { it is ES6ExportDefaultAssignment }!!
          .firstChild { it is JSObjectLiteralExpression } as JSObjectLiteralExpression
      val computedAttr = obj.findProperty(COMPUTED_ATTRIBUTE)
      val propertyName = PROPERTY_NAME_PLACEHOLDER
      val newComputedPropertyString = """$propertyName(${arguments.joinToString(", ")}) {
              |  return ${el.text};
              |}""".trimMargin()
      val parentObject = if (computedAttr != null) {
        computedAttr.value as JSObjectLiteralExpression
      } else {
        obj
      }
      val newProperty =
          JSChangeUtil.createObjectLiteralPropertyFromText(
              if (computedAttr != null) {
                newComputedPropertyString
              } else {
                """computed: {
                     $newComputedPropertyString
                   }""".trimIndent()
              }, parentObject).insertAfter(parentObject.firstChild)

      if (parentObject.properties.size > 1) {
        newProperty.insertElementAfter(JSChangeUtil.createCommaPsiElement(parentObject))
      }
      val newPlaceholder = JSChangeUtil.createExpressionWithContext(if (arguments.isEmpty()) {
        propertyName
      } else {
        "$propertyName(${arguments.joinToString(", ")})"
      }, el)!!.psi
      el.firstChild.replace(newPlaceholder)
      val element = el.firstChild
      //      val newDescriptor = ProblemDescriptorImpl(element, element, "test",
      //          arrayOf(RenameIdentifierFix()), ProblemHighlightType.ERROR, false, null, false)
      //      RenameIdentifierFix().applyFix(project, newDescriptor)
      renameElement(element)
    }
  }

  override fun getFamilyName(): String {
    return "提取为计算属性"
  }

}

