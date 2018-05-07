/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.*
import com.intellij.psi.PsiPrimitiveType.*
import com.intellij.psi.javadoc.PsiDocComment
import com.wuhao.code.check.JUNIT_TEST_ANNOTATION_CLASS_NAME
import com.wuhao.code.check.Messages
import com.wuhao.code.check.ancestorOfType
import com.wuhao.code.check.ancestorsOfType
import com.wuhao.code.check.inspection.CodeFormatInspection
import com.wuhao.code.check.inspection.fix.ConsolePrintFix
import com.wuhao.code.check.inspection.fix.ExtractToVariableFix
import com.wuhao.code.check.inspection.fix.JavaBlockCommentFix
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType

/**
 * Java代码格式检查访问器
 * Created by 吴昊 on 18-4-26.
 * @author 吴昊
 * @since 1.1
 */
class JavaCodeFormatVisitor(holder: ProblemsHolder) : BaseCodeFormatVisitor(holder) {

  override fun support(language: Language): Boolean {
    return language == JavaLanguage.INSTANCE
  }

  override fun visitElement(element: PsiElement) {
    when (element) {
      is PsiClass -> {
        if (element.annotations.any { it.qualifiedName == "Entity" || it.qualifiedName == "Table" }) {
          element.fields.filter {
            !it.hasModifier(JvmModifier.STATIC) && it.hasModifier(JvmModifier.PRIVATE)
                && it.firstChild !is PsiDocComment
          }.forEach { fieldElement ->
            holder.registerProblem(fieldElement, Messages.commentRequired, ProblemHighlightType.GENERIC_ERROR,
                JavaBlockCommentFix())
          }
        }
      }
      is PsiIdentifier -> {
        //变量名不能少于2个字符
        if (element.text.length <= 1) {
          if (element.parent.getChildOfType<PsiTypeElement>() == null
              || element.parent.getChildOfType<PsiTypeElement>()!!.text != "Exception") {
//            holder.registerProblem(element, "变量名称不能少于2个字符", ProblemHighlightType.GENERIC_ERROR)
          }
        }
      }
      is PsiLiteralExpression -> {
        // 检查数字参数
        if (element.parent is PsiExpressionList
            && element.text.toUpperCase() !in listOf("0", "0L", "0F") && element.type in PRIMITIVE_TYPES) {
          holder.registerProblem(element, "不允许直接使用数字作为方法参数",
              ProblemHighlightType.GENERIC_ERROR,
              ExtractToVariableFix())
        }
      }
      is PsiMethodCallExpression -> {
        // 使用日志输入代替System.out
        if (element.text.startsWith("System.out") || element.text.startsWith("System.err")) {
          if (element.ancestorOfType<PsiMethod>() == null
              || !element.ancestorsOfType<PsiMethod>().any { func ->
                func.annotations.any { annotation ->
                  annotation.qualifiedName == JUNIT_TEST_ANNOTATION_CLASS_NAME
                }
              }
          ) {
            holder.registerProblem(element, "使用日志向控制台输出", ProblemHighlightType.GENERIC_ERROR, ConsolePrintFix())
          }
        }
      }
      is PsiMethod -> {
        // 方法长度不能超过指定长度
        if (element.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FUNCTION) {
          holder.registerProblem(element, "方法长度不能超过${CodeFormatInspection.MAX_LINES_PER_FUNCTION}行", ProblemHighlightType.GENERIC_ERROR)
        }
        // 接口方法必须包含注释
        val elClass = element.containingClass
        if (elClass != null && elClass.isInterface && element.firstChild !is PsiDocComment) {
          holder.registerProblem(element, Messages.commentRequired, ProblemHighlightType.GENERIC_ERROR,
              JavaBlockCommentFix())
        }
      }
    }
  }

  companion object {

    val PRIMITIVE_TYPES = setOf(LONG, INT, DOUBLE, FLOAT, BYTE, SHORT)
  }
}
