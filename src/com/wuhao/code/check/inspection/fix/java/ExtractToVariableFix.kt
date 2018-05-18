/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix.java

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.parents
import com.wuhao.code.check.*

/**
 * 将方法直接引用的数值或字符串参数提取为变量
 * Created by 吴昊 on 18-4-26.
 * @author 吴昊
 * @since 1.1
 */
class ExtractToVariableFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.endElement as PsiLiteralExpression
    val factory = el.psiElementFactory
    val name = resolveParameterName(el)
    val statement = el.parents().firstOrNull { it is PsiExpressionStatement || it is PsiField }
    if (statement != null) {
      when (statement) {
        is PsiField -> {
          val newField = factory.createFieldFromText("""${statement.modifiers.joinToString(" ").toLowerCase()} ${el.type!!
              .presentableText} $name = ${el.text};""", null)
          statement.insertElementsBefore(newField, getNewLine(project))
        }
        else -> {
          val declarationStatement = factory.createVariableDeclarationStatement(name, el.type!!, el)
          statement.insertElementsBefore(declarationStatement, getNewLine(project))
        }
      }
      val newArgument = el.replace(factory.createIdentifier(name))
      renameElement(newArgument, -1, newArgument.parent, newArgument.parent.children.indexOf(newArgument))
    }
  }

  override fun getFamilyName(): String {
    return "提取为变量"
  }

  private fun resolveParameterName(el: PsiLiteralExpression): String {
    val parameters = el.ancestorOfType<PsiExpressionList>()
    if (parameters != null) {
      val parameterIndex = parameters.expressions.indexOf(el)
      val methodExpression = el.parents().firstOrNull { it is PsiMethodCallExpression } as PsiMethodCallExpression?
      if (methodExpression != null) {
        val calledMethod = methodExpression.resolveMethod()
        if (calledMethod != null && parameters.expressionCount == calledMethod.parameters.size - 1) {
          return calledMethod.parameters[parameterIndex].name ?: PROPERTY_NAME_PLACEHOLDER
        }
      }
    }
    return PROPERTY_NAME_PLACEHOLDER
  }

}

