/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.*
import com.wuhao.code.check.inspection.CodeFormatInspection
import com.wuhao.code.check.inspection.fix.KotlinCommaFix
import com.wuhao.code.check.inspection.fix.KotlinCommentQuickFix
import com.wuhao.code.check.style.arrangement.kotlin.KotlinRecursiveVisitor
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import java.util.logging.Logger

/**
 * Created by 吴昊 on 18-4-26.
 */
class KotlinCodeFormatVisitor(val holder: ProblemsHolder) : KotlinRecursiveVisitor(), BaseCodeFormatVisitor {

  override fun support(language: Language): Boolean {
    return language == KotlinLanguage.INSTANCE
  }

  override fun visitConstantExpression(expression: KtConstantExpression, data: Any?) {
    // 不能使用未声明的数字作为参数
    if (expression.parent is KtValueArgument
        && expression.parent.getChildOfType<KtValueArgumentName>() == null
        && expression.text !in listOf("0", "1", "2", "3", "4", "5")
        && expression.text.matches("\\d+".toRegex())) {
      holder.registerProblem(expression, "不得直接使用未经声明的数字作为变量", ERROR)
    }
  }

  override fun visitElement(element: PsiElement) {
    when (element) {
      is LeafPsiElement -> {
        // 检查变量名称，不得少于2个字符
        if ((element.text == "val" || element.text == "var")
            && element.elementType is KtKeywordToken) {
          val paramNameLength = element.nextSibling?.nextSibling?.text?.length
          if (paramNameLength != null && paramNameLength <= 1) {
//            holder.registerProblem(element.nextSibling.nextSibling, "变量名称不得少于两个字符", ProblemHighlightType.ERROR)
          }
        }
        // Kotlin中不需要使用分号
        if (element.text == ";" && element.parent !is KtLiteralStringTemplateEntry) {
          holder.registerProblem(element, "Kotlin中代码不需要以;结尾",
              ERROR, KotlinCommaFix())
        }
      }
    }
  }

  override fun visitNamedFunction(function: KtNamedFunction, data: Any?) {
    // 一等方法必须添加注释
    if (function.parent is KtFile && function.firstChild !is KDoc) {
      holder.registerProblem(function, "一等方法必须添加注释", ERROR, KotlinCommentQuickFix())
    }
    // 接口方法必须添加注释
    val containingClass = function.containingClass()
    if (containingClass != null && containingClass.isInterface()
        && function.firstChild !is KDoc) {
      holder.registerProblem(if (function.nameIdentifier != null) {
        function.nameIdentifier!!
      } else {
        function
      }, "接口方法必须添加注释", ERROR,
          KotlinCommentQuickFix())
    }
    // 方法长度不能超过指定长度
    if (function.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FUNCTION) {
      holder.registerProblem(function, "方法长度不能超过${CodeFormatInspection.MAX_LINES_PER_FUNCTION}行", GENERIC_ERROR)
    }
  }

  override fun visitProperty(property: KtProperty, data: Any?) {
    // 一等属性(非private)必须添加注释
    if (property.isFirstLevelProperty() && !property.hasDocComment()
        && !property.hasModifier(KtTokens.PRIVATE_KEYWORD)) {
      holder.registerProblem(property, Messages.commentRequired, GENERIC_ERROR,
          KotlinCommentQuickFix())
    }
    // data类字段必须添加注释
    if (property.parent != null && property.parent is KtClassBody
        && property.containingClass()!!.isData()
        && property.firstChild !is KDoc) {
      holder.registerProblem(property, Messages.commentRequired, GENERIC_ERROR, KotlinCommentQuickFix())
    }
  }

  override fun visitReferenceExpression(expression: KtReferenceExpression, data: Any?) {
    // 使用日志输入代替System.out
    if (expression.text == "println") {
      if (expression.ancestorOfType<KtFunction>() == null
          || !expression.ancestorsOfType<KtFunction>().any { func ->
            func.annotationEntries.map { annoEntry ->
              annoEntry.toLightAnnotation()
            }.any { lightAnnotation ->
              lightAnnotation?.qualifiedName == JUNIT_TEST_ANNOTATION_CLASS_NAME
            }
          }
      ) {
        holder.registerProblem(expression, "使用日志向控制台输出", ERROR)
      }
    }
  }

  companion object {
    val LOG: Logger = Logger.getLogger("Inspection")
  }
}

