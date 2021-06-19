/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix

import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.core.replaced

/**
 * javascript对象属性排序
 * @author 吴昊
 * @since 1.1
 */
class SchemaFormFieldsTypeFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    JsRecursiveVisitor().visitElement(element)
    ReformatCodeProcessor(descriptor.psiElement.containingFile, true).run()
  }

  override fun getFamilyName(): String {
    return "转为数组属性"
  }

  class JsRecursiveVisitor : JSElementVisitor() {

    override fun visitElement(element: PsiElement) {
      super.visitElement(element)
      element.children.forEach {
        it.accept(this)
      }
    }

    override fun visitJSObjectLiteralExpression(node: JSObjectLiteralExpression) {
      if (node.parent is JSProperty && (node.parent as JSProperty).name == "fields") {
        val str = node.properties.joinToString(",\n") {
          val propertyValue = it.value as JSObjectLiteralExpression
          if (propertyValue.properties.none { it.name == "property" }) {
            val exp = JSChangeUtil.createObjectLiteralPropertyFromText("property: '${it.name}'", it)
            propertyValue.addAfter(exp, propertyValue.firstChild)
          }
          propertyValue.text
        }
        val newElement = JSChangeUtil.createExpressionWithContext("[\n$str\n]", node)!!.psi
        node.replaced(newElement)
      }
      super.visitJSObjectLiteralExpression(node)
    }

  }

}
