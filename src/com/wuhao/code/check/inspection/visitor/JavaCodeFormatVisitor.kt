/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.*
import com.intellij.psi.JavaTokenType.*
import com.wuhao.code.check.*
import com.wuhao.code.check.inspection.CodeFormatInspection
import com.wuhao.code.check.inspection.fix.SpaceQuickFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Type.Before
import com.wuhao.code.check.inspection.fix.java.CamelCaseFix
import com.wuhao.code.check.inspection.fix.java.ExtractToVariableFix
import com.wuhao.code.check.inspection.fix.java.JavaConsolePrintFix
import org.jetbrains.kotlin.idea.quickfix.RenameIdentifierFix
import org.jetbrains.kotlin.idea.refactoring.getLineCount

/**
 * Java代码格式检查访问器
 * Created by 吴昊 on 18-4-26.
 * @author 吴昊
 * @since 1.1
 */
class JavaCodeFormatVisitor(val holder: ProblemsHolder) :
    JavaElementVisitor(), BaseCodeFormatVisitor {

  companion object {

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
        val place = when (position) {
          Before -> "前面"
          else -> "后面"
        }
        val check = when (position) {
          Before -> checkElement.prevSibling
          else -> checkElement.nextSibling
        }
        if (check !is PsiWhiteSpace) {
          holder.registerError(checkElement, "${place}应当有空格", fix)
        } else if (check.textLength != 1) {
          holder.registerError(checkElement, "${place}应当只有一个空格", fix)
        }
      }
    }

  }

  override fun support(language: Language): Boolean {
    return language == JavaLanguage.INSTANCE
  }

  override fun visitFile(file: PsiFile) {
    if (file.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FILE) {
      holder.registerError(file, "文件长度不允许超过${CodeFormatInspection.MAX_LINES_PER_FILE}行")
    }
  }

  override fun visitForStatement(statement: PsiForStatement) {
    shouldHaveSpaceBeforeOrAfter(statement.condition, holder)
    shouldHaveSpaceBeforeOrAfter(statement.update, holder)
    shouldHaveSpaceBeforeOrAfter(statement.rParenth, holder, SpaceQuickFix.Type.After)
  }

  override fun visitIdentifier(identifier: PsiIdentifier) {
    // 方法名、字段名长度不能少于2个字符
    if (identifier.text.length <= 1
        && identifier.parent !is PsiTypeParameter) {
      if ((identifier.parent is PsiMethod || identifier.parent is PsiClass)
          || (identifier.parent is PsiField && identifier.getAncestor(2) is PsiClass)) {
        holder.registerError(identifier, Messages.nameMustNotLessThan2Chars, RenameIdentifierFix())
      }
    }
    val namedElement = identifier.parent
    if (namedElement is PsiMethod || namedElement is PsiClass || namedElement is PsiVariable) {
      if (!identifier.text.isCamelCase) {
        holder.registerError(identifier, "命名格式错误，格式必须符合驼峰命名法", CamelCaseFix())
      }
    }
  }

  override fun visitIfStatement(statement: PsiIfStatement) {
    shouldHaveSpaceBeforeOrAfter(statement.rParenth, holder, SpaceQuickFix.Type.After)
  }

  override fun visitLiteralExpression(expression: PsiLiteralExpression) {
    // 检查数字参数
    if (expression.parent is PsiExpressionList
        && expression.firstChild.node.elementType in listOf(
            INTEGER_LITERAL, LONG_LITERAL, FLOAT_LITERAL,
            DOUBLE_LITERAL, STRING_LITERAL)
        && expression.text.length > 1) {
      if (expression.firstChild.node.elementType != STRING_LITERAL
          || expression.textLength > MAX_STRING_ARGUMENT_LENGTH) {
        holder.registerError(expression, Messages.noConstantArgument,
            ExtractToVariableFix())
      }
    }
  }

  override fun visitMethod(method: PsiMethod) {
    // 方法长度不能超过指定长度
    if (method.nameIdentifier != null && method.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FUNCTION) {
      holder.registerError(method.nameIdentifier!!,
          "方法长度不能超过${CodeFormatInspection.MAX_LINES_PER_FUNCTION}行")
    }
  }

  override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
    // 使用日志输入代替System.out
    if (expression.text.startsWith("System.out") || expression.text.startsWith("System.err")) {
      if (expression.ancestorOfType<PsiMethod>() == null
          || !expression.getAncestorsOfType<PsiMethod>().any { func ->
            func.annotations.any { annotation ->
              annotation.qualifiedName == JUNIT_TEST_ANNOTATION_CLASS_NAME
            }
          }
      ) {
        holder.registerError(expression, "使用日志向控制台输出", JavaConsolePrintFix())
      }
    }
  }

}

