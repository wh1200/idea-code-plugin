/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.jvm.JvmModifier.FINAL
import com.intellij.lang.jvm.JvmModifier.STATIC
import com.intellij.psi.*
import com.intellij.psi.JavaTokenType.*
import com.intellij.psi.impl.source.PsiClassImpl
import com.wuhao.code.check.ancestorOfType
import com.wuhao.code.check.constants.*
import com.wuhao.code.check.enums.NamingMethod
import com.wuhao.code.check.enums.NamingMethod.*
import com.wuhao.code.check.getAncestor
import com.wuhao.code.check.getAncestorsOfType
import com.wuhao.code.check.getLineCount
import com.wuhao.code.check.inspection.fix.SpaceQuickFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Position.Before
import com.wuhao.code.check.inspection.fix.java.JavaConsolePrintFix
import com.wuhao.code.check.inspection.fix.java.JavaElementNameFix
import org.jetbrains.kotlin.idea.quickfix.RenameIdentifierFix

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
                                     position: SpaceQuickFix.Position = Before) {
      if (checkElement != null) {
        val fix = SpaceQuickFix(position)
        val place = when (position) {
          Before -> "前面"
          else   -> "后面"
        }
        val check = when (position) {
          Before -> checkElement.prevSibling
          else   -> checkElement.nextSibling
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
    if (file.getLineCount() > MAX_LINES_PER_FILE) {
      holder.registerError(file, "文件长度不允许超过${MAX_LINES_PER_FILE}行")
    }
  }

  override fun visitForStatement(statement: PsiForStatement) {
    shouldHaveSpaceBeforeOrAfter(statement.condition, holder)
    shouldHaveSpaceBeforeOrAfter(statement.update, holder)
    shouldHaveSpaceBeforeOrAfter(statement.rParenth, holder, SpaceQuickFix.Position.After)
  }

  override fun visitIdentifier(identifier: PsiIdentifier) {
    // 方法名、字段名长度不能少于2个字符
    if (identifier.text.length <= 1 && identifier.parent !is PsiTypeParameter) {
      // for循环中的变量可以为单字符
      if (identifier.parent is PsiLocalVariable && identifier.getAncestor(2) is PsiDeclarationStatement
          && identifier.getAncestor(3) is PsiForStatement) {
      } else if ((identifier.parent is PsiParameter || identifier.parent is PsiParameterList)
          || (identifier.parent is PsiMethod || identifier.parent is PsiClass)
          || (identifier.parent is PsiField && identifier.getAncestor(2) is PsiClass)) {
        holder.registerError(identifier, Messages.NAME_MUST_NOT_LESS_THAN2_CHARS, RenameIdentifierFix())
      }
    }
    val namedElement = identifier.parent
    if (namedElement is PsiClassImpl || namedElement is PsiEnumConstant) {
      identifier.checkNaming(Pascal)
    } else if (namedElement is PsiField || namedElement is PsiLocalVariable
        || (namedElement is PsiMethod && !namedElement.isConstructor)) {
      if (namedElement is PsiField
          && namedElement.hasModifier(STATIC)
          && namedElement.hasModifier(FINAL)) {
        identifier.checkNaming(Constant)
      } else {
        identifier.checkNaming(Camel)
      }
    }
  }

  override fun visitIfStatement(statement: PsiIfStatement) {
    shouldHaveSpaceBeforeOrAfter(statement.rParenth, holder, SpaceQuickFix.Position.After)
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
//        holder.registerError(expression, Messages.NO_CONSTANT_ARGUMENT, ExtractToVariableFix())
      }
    }
  }

  override fun visitMethod(method: PsiMethod) {
    // 方法长度不能超过指定长度
    if (method.nameIdentifier != null && method.getLineCount() > MAX_LINES_PER_FUNCTION) {
      holder.registerError(method.nameIdentifier!!,
          "方法长度不能超过${MAX_LINES_PER_FUNCTION}行")
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

  private fun PsiIdentifier.checkNaming(method: NamingMethod) {
    if (!method.test(this.text)) {
      holder.registerError(this, "命名格式错误，格式必须符合${method.zhName}命名法", JavaElementNameFix(method))
    }
  }

}

