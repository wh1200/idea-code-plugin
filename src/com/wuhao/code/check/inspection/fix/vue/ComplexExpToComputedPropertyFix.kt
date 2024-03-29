package com.wuhao.code.check.inspection.fix.vue

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.dialects.TypeScriptJSXLanguageDialect
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClassExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil.SCRIPT_TAG_NAME
import com.wuhao.code.check.*
import com.wuhao.code.check.constants.PROPERTY_NAME_PLACEHOLDER
import com.wuhao.code.check.inspection.visitor.VueCodeFormatVisitor.Companion.COMPUTED_ATTRIBUTE
import com.wuhao.code.check.lang.vue.VueDirectives.FOR
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import java.util.*

/**
 * vue组件模板中，引用的复杂js表达式导出为计算属性的修复
 * @author 吴昊
 * @since 1.3.5
 */
class ComplexExpToComputedPropertyFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.psiElement
    val forVariables = el.getAncestorsOfType<XmlTag>()
        .asSequence()
        .filter { it.getAttribute(FOR) != null }
        .map {
          it.getAttribute(FOR)?.valueElement?.getChildOfType<JSEmbeddedContent>()?.firstChild as
              VueJSVForExpression?
        }.map { it?.getVarStatement()?.variables?.toList() }.filterNotNull()
        .flatten().map { it.name }.filterNotNull()
        .toList()
    val arguments = sortedSetOf<String>()

    val script = el.parent.ancestorOfType<XmlDocument>()!!
        .firstChild { it is XmlTag && it.name == SCRIPT_TAG_NAME }
    if (script != null) {
      val embeddedContent = script.firstChild { it is JSEmbeddedContent }!!
      val es6ExportDefaultAssignment = embeddedContent.firstChild { it is ES6ExportDefaultAssignment }!!
      val exp = es6ExportDefaultAssignment.getChildOfType<JSObjectLiteralExpression>()
      if (exp != null) {
        fixOnObject(exp, arguments, el, forVariables)
      } else {
        val tsClass = es6ExportDefaultAssignment.getChildOfType<TypeScriptClassExpression>()
        if (tsClass != null) {
          fixOnTsClass(tsClass, arguments, el, forVariables)

        }
      }
    }
  }

  override fun getFamilyName(): String {
    return "提取为计算属性"
  }

  private fun addThis(
      forVariables: List<String>,
      arguments: TreeSet<String>,
      el: PsiElement
  ) {
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
  }

  private fun createNewPlaceHolder(
      arguments: TreeSet<String>,
      propertyName: String,
      el: PsiElement
  ): PsiElement {
    return JSChangeUtil.createExpressionWithContext(
        if (arguments.isEmpty()) {
          propertyName
        } else {
          "$propertyName(${arguments.joinToString(", ")})"
        }, el
    )!!.psi
  }

  private fun fixOnObject(
      exp: JSObjectLiteralExpression,
      arguments: TreeSet<String>,
      el: PsiElement,
      forVariables: List<String>
  ) {
    addThis(forVariables, arguments, el)
    val computedAttr = exp.findProperty(COMPUTED_ATTRIBUTE)
    val propertyName = PROPERTY_NAME_PLACEHOLDER
    val newComputedPropertyString = """$propertyName(${arguments.joinToString(", ")}) {
                |  return ${el.text};
                |}""".trimMargin()
    val parentObject = if (computedAttr != null) {
      computedAttr.value as JSObjectLiteralExpression
    } else {
      exp
    }
    val newProperty =
        JSChangeUtil.createObjectLiteralPropertyFromText(
            if (computedAttr != null) {
              newComputedPropertyString
            } else {
              """computed: {
                       $newComputedPropertyString
                     }""".trimIndent()
            }, parentObject
        ).insertAfter(parentObject.firstChild)

    if (parentObject.properties.size > 1) {
      newProperty.insertElementAfter(JSChangeUtil.createCommaPsiElement(parentObject))
    }
    val newPlaceholder = createNewPlaceHolder(arguments, propertyName, el)
    el.firstChild.replace(newPlaceholder)
    val element = el.firstChild
    renameElement(element)
  }

  private fun fixOnTsClass(
      cls: TypeScriptClassExpression,
      arguments: TreeSet<String>,
      el: PsiElement,
      forVariables: List<String>
  ) {
    val name = Messages.showInputDialog("请输入计算属性名称", "提取计算属性", null)
    if (name != null) {
      addThis(forVariables, arguments, el)
      val newComputedPropertyString = """
      |get ${name}(${arguments.joinToString(", ")}) {
      |  return ${el.text};
      |}""".trimMargin()

      val exp = PsiFileFactory.getInstance(el.project)
          .createFileFromText(
              TypeScriptJSXLanguageDialect.findInstance(
                  TypeScriptJSXLanguageDialect::class.java
              ), """class TmpClass {
          |
          |    $newComputedPropertyString
          |
          |}""".trimMargin()
          )
      val field = (exp.firstChild as TypeScriptClass).children[2] as TypeScriptFunction
      cls.addBefore(field, cls.lastChild)
      val newPlaceholder = createNewPlaceHolder(arguments, name, el)
      el.replace(newPlaceholder)
    }
  }

}
