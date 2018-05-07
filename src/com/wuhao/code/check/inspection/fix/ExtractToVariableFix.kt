/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiExpressionStatement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.impl.PsiElementFactoryImpl
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.util.parents
import com.wuhao.code.check.insertElementBefore
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

/**
 * 将方法直接引用的数值或字符串参数提取为变量
 * Created by 吴昊 on 18-4-26.
 * @author 吴昊
 * @since 1.1
 */
class ExtractToVariableFix : LocalQuickFix {

  override fun getFamilyName(): String {
    return "提取为变量"
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.endElement as PsiLiteralExpression
    val factory = PsiElementFactoryImpl(PsiManagerEx.getInstanceEx(el.project))
    val name = resolveParameterName(el)
    val statement = el.parents().firstOrNull { it is PsiExpressionStatement || it is PsiField }
    if (statement != null) {
      if (statement is PsiField) {
        val newField = factory.createFieldFromText("""${statement.modifiers.joinToString(" ").toLowerCase()} ${el.type!!
            .presentableText} $name = ${el.text};""", null)
        statement.insertElementBefore(newField)
      } else {
        val declarationStatement = factory.createVariableDeclarationStatement(name, el.type!!, el)
        statement.insertElementBefore(declarationStatement)
      }
      el.replace(factory.createIdentifier(name))
    }
  }

  private fun resolveParameterName(el: PsiLiteralExpression): String {
    val methodExpression = el.parents().firstOrNull { it is PsiMethodCallExpression } as PsiMethodCallExpression?
    if (methodExpression != null) {
      val calledMethod = methodExpression.resolveMethod()
      val parameterIndex = el.parent.getChildrenOfType<PsiLiteralExpression>().indexOf(el)
      if (calledMethod != null && parameterIndex <= calledMethod.parameterList.parameters.size - 1) {
        return calledMethod.parameterList.parameters[parameterIndex].name ?: "tmp"
      }
    }
    return "tmp"
  }

}

