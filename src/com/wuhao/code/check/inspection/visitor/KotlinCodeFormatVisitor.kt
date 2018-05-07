/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.*
import com.wuhao.code.check.inspection.CodeFormatInspection
import com.wuhao.code.check.inspection.fix.KotlinCommaFix
import com.wuhao.code.check.inspection.fix.KotlinCommentQuickFix
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
class KotlinCodeFormatVisitor(holder: ProblemsHolder) : BaseCodeFormatVisitor(holder) {

  override fun support(language: Language): Boolean {
    return language == KotlinLanguage.INSTANCE
  }

  override fun visitElement(element: PsiElement) {
    when (element) {
      is KtProperty -> {
        // 一等属性(非private)必须添加注释
        if (element.isFirstLevelProperty() && !element.hasDocComment()
            && !element.hasModifier(KtTokens.PRIVATE_KEYWORD)) {
          holder.registerProblem(element, Messages.commentRequired, ProblemHighlightType.GENERIC_ERROR,
              KotlinCommentQuickFix())
        }
        // data类字段必须添加注释
        if (element.parent != null && element.parent is KtClassBody
            && element.containingClass()!!.isData()
            && element.firstChild !is KDoc) {
          holder.registerProblem(element, Messages.commentRequired, ProblemHighlightType.GENERIC_ERROR, KotlinCommentQuickFix())
        }
      }
      is KtFunction -> {
        // 一等方法必须添加注释
        if (element.parent is KtFile && element.firstChild !is KDoc) {
          holder.registerProblem(element, "一等方法必须添加注释", ProblemHighlightType.GENERIC_ERROR, KotlinCommentQuickFix())
        }
        // 接口方法必须添加注释
        val containingClass = element.containingClass()
        if (containingClass != null && containingClass.isInterface()
            && element.firstChild !is KDoc) {
          holder.registerProblem(element, "接口方法必须添加注释", ProblemHighlightType.GENERIC_ERROR, KotlinCommentQuickFix())
        }
        // 方法长度不能超过指定长度
        if (element.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FUNCTION) {
          holder.registerProblem(element, "方法长度不能超过${CodeFormatInspection.MAX_LINES_PER_FUNCTION}行", ProblemHighlightType.GENERIC_ERROR)
        }
      }
      is KtReferenceExpression -> {
        // 使用日志输入代替System.out
        if (element.text == "println") {
          if (element.ancestorOfType<KtFunction>() == null
              || !element.ancestorsOfType<KtFunction>().any { func ->
                func.annotationEntries.map { annoEntry ->
                  annoEntry.toLightAnnotation()
                }.any { lightAnnotation ->
                  lightAnnotation?.qualifiedName == JUNIT_TEST_ANNOTATION_CLASS_NAME
                }
              }
          ) {
            holder.registerProblem(element, "使用日志向控制台输出", ProblemHighlightType.GENERIC_ERROR)
          }
        }
      }
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
              ProblemHighlightType.ERROR, KotlinCommaFix())
        }

      }
      is KtConstantExpression -> {
        // 不能使用未声明的数字作为参数
        if (element.parent is KtValueArgument
            && element.parent.getChildOfType<KtValueArgumentName>() == null
            && element.text !in listOf("0", "1", "2", "3", "4", "5")
            && element.text.matches("\\d+".toRegex())) {
          holder.registerProblem(element, "不得直接使用未经声明的数字作为变量", ProblemHighlightType.ERROR)
        }
      }
    }
  }

  companion object {
    val LOG: Logger = Logger.getLogger("Inspection")
  }
}
