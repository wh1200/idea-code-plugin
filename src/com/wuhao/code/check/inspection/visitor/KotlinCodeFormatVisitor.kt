/**
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.*
import com.wuhao.code.check.inspection.CodeFormatInspection
import com.wuhao.code.check.inspection.fix.SpaceQuickFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Type.After
import com.wuhao.code.check.inspection.fix.kotlin.ExtractConstantToPropertyFix
import com.wuhao.code.check.inspection.fix.kotlin.KotlinCommaFix
import com.wuhao.code.check.inspection.fix.kotlin.KotlinConsolePrintFix
import com.wuhao.code.check.inspection.visitor.JavaCodeFormatVisitor.Companion.shouldHaveSpaceBeforeOrAfter
import org.jetbrains.kotlin.KtNodeTypes.*
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType

/**
 * Created by 吴昊 on 18-4-26.
 */
class KotlinCodeFormatVisitor(val holder: ProblemsHolder) : KtVisitor<Any, Any>(), BaseCodeFormatVisitor {

  override fun support(language: Language): Boolean {
    return language == KotlinLanguage.INSTANCE
  }

  override fun visitClassBody(classBody: KtClassBody, data: Any?) {
    if (classBody.prevSibling !is PsiWhiteSpace) {
      holder.registerError(classBody.lBrace!!, "前面应当有空格", SpaceQuickFix(SpaceQuickFix.Type.BeforeParent))
    }
    super.visitClassBody(classBody, data)
  }

  override fun visitConstantExpression(expression: KtConstantExpression, data: Any?) {
    // 不能使用未声明的数字作为参数
    if (expression.parent is KtValueArgument
        && expression.node.elementType in listOf(INTEGER_CONSTANT, FLOAT_CONSTANT, STRING_TEMPLATE)
        && expression.parent.getChildOfType<KtValueArgumentName>() == null
        && expression.textLength > 1) {
      if (expression.node.elementType != STRING_TEMPLATE || expression.textLength >= MAX_STRING_ARGUMENT_LENGTH) {
        holder.registerError(expression, Messages.noConstantArgument, ExtractConstantToPropertyFix())
      }
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
            //            holder.registerError(element.nextSibling.nextSibling, "变量名称不得少于两个字符")
          }
        }
        // Kotlin中不需要使用分号
        if (element.text == ";" && element.parent !is KtLiteralStringTemplateEntry
            && element.parent !is KtEnumEntry) {
          holder.registerError(element, "Kotlin中代码不需要以;结尾",
              KotlinCommaFix())
        }
      }
    }
  }

  override fun visitFile(file: PsiFile) {
    if (file.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FILE) {
      holder.registerError(file, "文件长度不允许超过${CodeFormatInspection.MAX_LINES_PER_FILE}行")
    }
  }

  override fun visitForExpression(expression: KtForExpression, data: Any?) {
    shouldHaveSpaceBeforeOrAfter(expression.rightParenthesis, holder, After)
  }

  override fun visitIfExpression(expression: KtIfExpression, data: Any?) {
    shouldHaveSpaceBeforeOrAfter(expression.rightParenthesis, holder, After)
  }

  override fun visitNamedFunction(function: KtNamedFunction, data: Any?) {
    // 方法长度不能超过指定长度
    if (function.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FUNCTION) {
      if (function.nameIdentifier != null) {
        holder.registerError(function.nameIdentifier!!, "方法长度不能超过${CodeFormatInspection.MAX_LINES_PER_FUNCTION}行")
      } else {
        holder.registerError(function, "方法长度不能超过${CodeFormatInspection.MAX_LINES_PER_FUNCTION}行")
      }
    }
  }

  override fun visitReferenceExpression(expression: KtReferenceExpression, data: Any?) {
    // 使用日志输入代替System.out
    if (expression.text == "println") {
      if (expression.ancestorOfType<KtFunction>() == null
          || !expression.getAncestorsOfType<KtFunction>().any { func ->
            func.annotationEntries.map { annotationEntry ->
              annotationEntry.toLightAnnotation()
            }.any { lightAnnotation ->
              lightAnnotation?.qualifiedName == JUNIT_TEST_ANNOTATION_CLASS_NAME
            }
          }
      ) {
        holder.registerError(expression, "使用日志向控制台输出", KotlinConsolePrintFix())
      }
    }
  }

}

