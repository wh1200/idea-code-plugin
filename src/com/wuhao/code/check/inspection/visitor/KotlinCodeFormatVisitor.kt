/**
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.ide.DataManager
import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.refactoring.actions.RenameElementAction
import com.wuhao.code.check.*
import com.wuhao.code.check.inspection.CodeFormatInspection
import com.wuhao.code.check.inspection.fix.SpaceQuickFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Type.After
import com.wuhao.code.check.inspection.fix.kotlin.KotlinCommaFix
import com.wuhao.code.check.inspection.visitor.JavaCodeFormatVisitor.Companion.shouldHaveSpaceBeforeOrAfter
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.core.moveCaret
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * Created by 吴昊 on 18-4-26.
 */
class KotlinCodeFormatVisitor(val holder: ProblemsHolder) : KtVisitor<Any, Any>(), BaseCodeFormatVisitor {

  override fun support(language: Language): Boolean {
    return language == KotlinLanguage.INSTANCE
  }

  override fun visitClassBody(classBody: KtClassBody, data: Any?) {
    if (classBody.prevSibling !is PsiWhiteSpace) {
      holder.registerProblem(classBody.lBrace!!, "前面应当有空格", ERROR, SpaceQuickFix(SpaceQuickFix.Type.BeforeParent))
    }
    super.visitClassBody(classBody, data)
  }

  override fun visitConstantExpression(expression: KtConstantExpression, data: Any?) {
    // 不能使用未声明的数字作为参数
    if (expression.parent is KtValueArgument
        && expression.parent.getChildOfType<KtValueArgumentName>() == null
        && expression.text !in listOf("0", "1", "2", "3", "4", "5")
        && expression.text.matches("\\d+".toRegex())) {
      holder.registerProblem(expression, "不得直接使用未经声明的数字作为变量", ERROR, object : LocalQuickFix {

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
          val file = descriptor.psiElement.containingFile
          val constant = descriptor.psiElement as KtConstantExpression
          //获取包含数值参数的当前表达式
          val exp = descriptor.psiElement.getAncestor(2)!!
              .getContinuousAncestorsMatches<KtExpression> {
                it is KtCallExpression
                    || it is KtQualifiedExpression || it is KtProperty
              }.last()
          val propertyName = "_tmp"
          val factory = KtPsiFactory(project)
          val property = factory.createProperty("val $propertyName = ${constant.text}")
          val newProperty = property.insertBefore(exp)
          val newValueArgument = constant.parent
              .replace(factory.createArgument(propertyName))
          newProperty.insertElementAfter(newLine)
          val action = RenameElementAction()
          val context = DataManager.getInstance()
              .dataContextFromFocus.result
          val editor = context.getData(CommonDataKeys.EDITOR)!!
          editor.moveCaret(newValueArgument.startOffset)
          val handler = action.getHandler(context)
          if (handler != null) {
            handler.invoke(project, editor, file, context)
          }
        }

        override fun getFamilyName(): String {
          return "提取为变量"
        }

      })
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
        if (element.text == ";" && element.parent !is KtLiteralStringTemplateEntry
            && element.parent !is KtEnumEntry) {
          holder.registerProblem(element, "Kotlin中代码不需要以;结尾",
              ERROR, KotlinCommaFix())
        }
      }
    }
  }

  override fun visitFile(file: PsiFile) {
    if (file.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FILE) {
      holder.registerProblem(file, "文件长度不允许超过${CodeFormatInspection.MAX_LINES_PER_FILE}行", ERROR)
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
        holder.registerProblem(function.nameIdentifier!!, "方法长度不能超过${CodeFormatInspection.MAX_LINES_PER_FUNCTION}行",
            ERROR)
      } else {
        holder.registerProblem(function, "方法长度不能超过${CodeFormatInspection.MAX_LINES_PER_FUNCTION}行", ERROR)
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
        holder.registerProblem(expression, "使用日志向控制台输出", ERROR)
      }
    }
  }

}

