/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.*
import com.intellij.psi.PsiPrimitiveType.*
import com.intellij.psi.javadoc.PsiDocComment
import com.wuhao.code.check.*
import com.wuhao.code.check.inspection.CodeFormatInspection
import com.wuhao.code.check.inspection.fix.ConsolePrintFix
import com.wuhao.code.check.inspection.fix.ExtractToVariableFix
import com.wuhao.code.check.inspection.fix.JavaBlockCommentFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Type.Before
import org.jetbrains.kotlin.idea.refactoring.getLineCount

/**
 * Java代码格式检查访问器
 * Created by 吴昊 on 18-4-26.
 * @author 吴昊
 * @since 1.1
 */
class JavaCodeFormatVisitor(val holder: ProblemsHolder) :
    JavaElementVisitor(), BaseCodeFormatVisitor {

  override fun support(language: Language): Boolean {
    return language == JavaLanguage.INSTANCE
  }

  /**
   *
   * @param clazz 类元素
   */
  override fun visitClass(clazz: PsiClass) {
    if ((clazz !is PsiTypeParameter && clazz.firstChild == null || clazz.firstChild !is PsiDocComment) && clazz !is PsiAnonymousClass) {
      if (clazz.nameIdentifier != null) {
        holder.registerError(clazz.nameIdentifier!!, Messages.classCommentRequired, JavaBlockCommentFix())
      } else {
        holder.registerError(clazz, Messages.classCommentRequired, JavaBlockCommentFix())
      }
    }
    if (clazz.annotations.any { it.qualifiedName in listOf(ENTITY_CLASS, TABLE_CLASS) }) {
      clazz.fields.filter {
        !it.hasModifier(JvmModifier.STATIC) && it.hasModifier(JvmModifier.PRIVATE)
            && it.firstChild !is PsiDocComment
      }.forEach { fieldElement ->
        holder.registerProblem(fieldElement.nameIdentifier, Messages.commentRequired, ERROR,
            JavaBlockCommentFix())
      }
    }
  }

  override fun visitElement(element: PsiElement) {
  }

  override fun visitFile(file: PsiFile) {
    if (file.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FILE) {
      holder.registerProblem(file, "文件长度不允许超过${CodeFormatInspection.MAX_LINES_PER_FILE}行", ERROR)
    }
  }

  override fun visitForStatement(statement: PsiForStatement) {
    shouldHaveSpaceBeforeOrAfter(statement.condition, holder)
    shouldHaveSpaceBeforeOrAfter(statement.update, holder)
    shouldHaveSpaceBeforeOrAfter(statement.rParenth, holder, SpaceQuickFix.Type.After)
  }

  override fun visitIdentifier(identifier: PsiIdentifier) {
    // 方法名、字段名长度不能少于2个字符
    if (identifier.text.length <= 1) {
      if (identifier.parent is PsiMethod || identifier.parent is PsiClass) {
        holder.registerProblem(identifier, Messages.nameMustNotLessThan2Chars, ERROR)
      }
      if (identifier.parent is PsiField && identifier.parent.parent is PsiClass) {
        holder.registerProblem(identifier, Messages.nameMustNotLessThan2Chars, ERROR)
      }
    }
  }

  override fun visitIfStatement(statement: PsiIfStatement) {
    shouldHaveSpaceBeforeOrAfter(statement.rParenth, holder, SpaceQuickFix.Type.After)
  }

  override fun visitLiteralExpression(expression: PsiLiteralExpression) {
    // 检查数字参数
    if (expression.parent is PsiExpressionList
        && expression.text.toUpperCase() !in listOf("0", "0L", "0F") && expression.type in PRIMITIVE_TYPES) {
      holder.registerProblem(expression, "不允许直接使用数字作为方法参数",
          ERROR, ExtractToVariableFix())
    }
  }

  override fun visitMethod(method: PsiMethod) {
    // 方法长度不能超过指定长度
    if (method.nameIdentifier != null && method.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FUNCTION) {
      holder.registerProblem(method.nameIdentifier!!,
          "方法长度不能超过${CodeFormatInspection.MAX_LINES_PER_FUNCTION}行", ERROR)
    }
    // 接口方法必须包含注释
    val elClass = method.containingClass
    if (elClass != null && elClass.isInterface && method.firstChild !is PsiDocComment) {
      val elementToRegisterProblem = if (method.nameIdentifier != null) {
        method.nameIdentifier!!
      } else {
        method
      }
      holder.registerProblem(elementToRegisterProblem,
          Messages.interfaceMethodCommentRequired, ERROR, JavaBlockCommentFix())
    }
  }

  override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
    // 使用日志输入代替System.out
    if (expression.text.startsWith("System.out") || expression.text.startsWith("System.err")) {
      if (expression.ancestorOfType<PsiMethod>() == null
          || !expression.ancestorsOfType<PsiMethod>().any { func ->
            func.annotations.any { annotation ->
              annotation.qualifiedName == JUNIT_TEST_ANNOTATION_CLASS_NAME
            }
          }
      ) {
        holder.registerProblem(expression, "使用日志向控制台输出", ERROR, ConsolePrintFix())
      }
    }
  }

  companion object {

    const val ENTITY_CLASS = "javax.persistence.Entity"
    val PRIMITIVE_TYPES = setOf(LONG, INT, DOUBLE, FLOAT, BYTE, SHORT)
    const val TABLE_CLASS = "javax.persistence.Table"

    /**
     * 检查前面是否有空格
     * @param checkElement 被检查的元素
     * @param holder
     * @param position 检查空格的位置
     */
    fun shouldHaveSpaceBeforeOrAfter(checkElement: PsiElement?,
                                     holder: ProblemsHolder,
                                     position: SpaceQuickFix.Type = Before) {
      if (checkElement != null) {
        val fix = SpaceQuickFix(position)
        val place = if (position == Before) {
          "前面"
        } else {
          "后面"
        }
        val check = if (position == Before) {
          checkElement.prevSibling
        } else {
          checkElement.nextSibling
        }
        if (check !is PsiWhiteSpace) {
          holder.registerError(checkElement, "${place}应当有空格", fix)
        } else if (check.textLength != 1) {
          holder.registerError(checkElement, "${place}应当只有一个空格", fix)
        }
      }
    }

  }

}

