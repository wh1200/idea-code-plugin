/**
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.*
import com.wuhao.code.check.Messages.classCommentRequired
import com.wuhao.code.check.inspection.CodeFormatInspection
import com.wuhao.code.check.inspection.fix.DeleteFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Type.After
import com.wuhao.code.check.inspection.fix.kotlin.KotlinCommaFix
import com.wuhao.code.check.inspection.fix.kotlin.KotlinCommentQuickFix
import com.wuhao.code.check.inspection.visitor.JavaCodeFormatVisitor.Companion.shouldHaveSpaceBeforeOrAfter
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import java.util.logging.Logger

/**
 * Created by 吴昊 on 18-4-26.
 */
class KotlinCodeFormatVisitor(val holder: ProblemsHolder) : KtVisitor<Any, Any>(), BaseCodeFormatVisitor {

  override fun support(language: Language): Boolean {
    return language == KotlinLanguage.INSTANCE
  }

  override fun visitClass(klass: KtClass, data: Any?) {
    checkRedundantComment(klass)
    if (klass !is KtEnumEntry && klass.nameIdentifier != null) {
      if (klass.firstChild == null || klass.firstChild !is KDoc) {
        holder.registerError(klass.nameIdentifier!!, classCommentRequired, KotlinCommentQuickFix())
      }
    }
    super.visitClass(klass, data)
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
      holder.registerProblem(expression, "不得直接使用未经声明的数字作为变量", ERROR)
    }
  }

  override fun visitElement(element: PsiElement) {
    when (element) {
      is KDocSection -> {
        this.visitDocSection(element)
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
        if (element.text == ";" && element.parent !is KtLiteralStringTemplateEntry
            && element.parent !is KtEnumEntry) {
          holder.registerProblem(element, "Kotlin中代码不需要以;结尾",
              ERROR, KotlinCommaFix())
        }
      }
    }
    super.visitElement(element)
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
    checkRedundantComment(function)
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
      if (function.nameIdentifier != null) {
        holder.registerProblem(function.nameIdentifier!!, "方法长度不能超过${CodeFormatInspection.MAX_LINES_PER_FUNCTION}行",
            ERROR)
      } else {
        holder.registerProblem(function, "方法长度不能超过${CodeFormatInspection.MAX_LINES_PER_FUNCTION}行", ERROR)
      }
    }
  }

  override fun visitObjectDeclaration(declaration: KtObjectDeclaration, data: Any?) {
    if (!declaration.isCompanion() && declaration.nameIdentifier != null) {
      if (declaration.firstChild == null || declaration.firstChild !is KDoc) {
        holder.registerError(declaration.nameIdentifier!!, classCommentRequired, KotlinCommentQuickFix())
      }
    }
  }

  override fun visitPackageDirective(directive: KtPackageDirective, data: Any?) {
    checkRedundantComment(directive)
  }

  override fun visitProperty(property: KtProperty, data: Any?) {
    checkRedundantComment(property)
    // 一等属性(非private)必须添加注释
    if (property.isFirstLevelProperty() && !property.hasDocComment()
        && !property.hasModifier(KtTokens.PRIVATE_KEYWORD)) {
      registerPropertyCommentMissingError(property)
    }
    // data类字段必须添加注释
    if (property.parent != null && property.parent is KtClassBody
        && property.containingClass()!!.isData()
        && property.firstChild !is KDoc) {
      checkRedundantComment(property)
      registerPropertyCommentMissingError(property)
    }
  }

  override fun visitReferenceExpression(expression: KtReferenceExpression, data: Any?) {
    // 使用日志输入代替System.out
    if (expression.text == "println") {
      if (expression.ancestorOfType<KtFunction>() == null
          || !expression.ancestorsOfType<KtFunction>().any { func ->
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

  private fun checkRedundantComment(element: PsiElement) {
    if (element is KtPackageDirective) {
      fun registerErrorExceptFirst(list: List<PsiElement>) {
        list.reversed().forEachIndexed { index, comment ->
          if (index > 0) {
            holder.registerProblem(comment, Messages.redundantComment, ERROR, DeleteFix())
          }
        }
      }

      val docsBeforeDirective = element.getPrevContinuousSiblingsOfTypeIgnoreWhitespace<KDoc>()
      val commentsBeforeDirective = element.getPrevContinuousSiblingsOfTypeIgnoreWhitespace<PsiComment>()
      if (commentsBeforeDirective.size > 1) {
        registerErrorExceptFirst(commentsBeforeDirective)
      }
      if (docsBeforeDirective.size > 1) {
        registerErrorExceptFirst(docsBeforeDirective)
      }
    } else {
      if (element.firstChild is KDoc && element.prevSiblingIgnoreWhitespace is KDoc) {
        holder.registerProblem(element.prevSiblingIgnoreWhitespace!!, Messages.redundantComment, ERROR, DeleteFix())
      }
    }
  }

  private fun registerPropertyCommentMissingError(property: KtProperty) {
    if (property.nameIdentifier != null) {
      holder.registerProblem(property.nameIdentifier!!, Messages.commentRequired, ERROR,
          KotlinCommentQuickFix())
    } else {
      holder.registerProblem(property, Messages.commentRequired, ERROR,
          KotlinCommentQuickFix())
    }
  }

  private fun visitDocSection(section: KDocSection) {
    if (section.prevSiblingIgnoreWhitespace is LeafPsiElement
        && (section.prevSiblingIgnoreWhitespace as LeafPsiElement).elementType == KDocTokens.START) {
      val textElement = section.allChildren.firstOrNull { it is LeafPsiElement && it.elementType == KDocTokens.TEXT }
      if ((section.firstChild as LeafPsiElement).elementType == KDocTokens.LEADING_ASTERISK
          && section.firstChild.text != "*") {
        holder.registerProblem(section.firstChild, "应该为一个*", ERROR)
      } else if (textElement == section.firstChild) {
        holder.registerProblem(section.firstChild, "前面应该添加*", ERROR)
      } else if (textElement == null) {
        holder.registerProblem(section, "缺少注释内容", ERROR)
      }
    }
  }

  companion object {

    val LOG: Logger = Logger.getLogger("Inspection")

  }

}

