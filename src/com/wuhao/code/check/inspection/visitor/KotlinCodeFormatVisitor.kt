/**
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.*
import com.wuhao.code.check.constants.*
import com.wuhao.code.check.enums.NamingMethod
import com.wuhao.code.check.enums.NamingMethod.Camel
import com.wuhao.code.check.enums.NamingMethod.Constant
import com.wuhao.code.check.inspection.fix.SpaceQuickFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Position.After
import com.wuhao.code.check.inspection.fix.kotlin.KotlinCommaFix
import com.wuhao.code.check.inspection.fix.kotlin.KotlinConsolePrintFix
import com.wuhao.code.check.inspection.fix.kotlin.KotlinNameFix
import com.wuhao.code.check.inspection.visitor.JavaCodeFormatVisitor.Companion.shouldHaveSpaceBeforeOrAfter
import org.jetbrains.kotlin.KtNodeTypes.FOR
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.quickfix.RenameIdentifierFix
import org.jetbrains.kotlin.lexer.KtTokens.CONST_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

/**
 * kotlin代码格式检查访问器
 * Created by 吴昊 on 18/4/26.
 *
 * @author 吴昊
 * @since 1.1
 */
class KotlinCodeFormatVisitor(val holder: ProblemsHolder) : KtVisitor<Any, Any>(), BaseCodeFormatVisitor {

  override fun support(language: Language): Boolean {
    return language == KotlinLanguage.INSTANCE
  }

  override fun visitClassBody(classBody: KtClassBody, data: Any?) {
    if (classBody.prevSibling !is PsiWhiteSpace && classBody.lBrace != null) {
      holder.registerWarning(classBody.lBrace!!, "前面应当有空格", SpaceQuickFix(SpaceQuickFix.Position.BeforeParent))
    }
    super.visitClassBody(classBody, data)
  }

  override fun visitElement(element: PsiElement) {
    if (element.typeMatch(IDENTIFIER) && element.textLength <= 1 && psiElement().withAncestor(2, typePattern(FOR))
            .andNot(PsiPatterns2.leaf()).accepts(element)) {
  holder.registerWarning(element, Messages.NAME_MUST_NOT_LESS_THAN2_CHARS, KotlinCommaFix())
}
    when (element) {
      is LeafPsiElement -> {
        // Kotlin中不需要使用分号
        if (element.text == ";" && element.parent !is KtLiteralStringTemplateEntry
            && element.parent !is KtEnumEntry) {
          holder.registerWarning(element, "Kotlin中代码不需要以;结尾", KotlinCommaFix())
        }
      }
    }
  }

  override fun visitFile(file: PsiFile) {
    if (file.getLineCount() > MAX_LINES_PER_FILE) {
      holder.registerWarning(file, "文件长度不允许超过${MAX_LINES_PER_FILE}行")
    }
    return super.visitFile(file)
  }

  override fun visitForExpression(expression: KtForExpression, data: Any?) {
    shouldHaveSpaceBeforeOrAfter(expression.rightParenthesis, holder, After)
  }

  override fun visitIfExpression(expression: KtIfExpression, data: Any?) {
    shouldHaveSpaceBeforeOrAfter(expression.rightParenthesis, holder, After)
  }

  override fun visitNamedFunction(function: KtNamedFunction, data: Any?) {
    // 方法长度不能超过指定长度
    if (function.getLineCount() > MAX_LINES_PER_FUNCTION) {
      if (function.nameIdentifier != null) {
        holder.registerWarning(function.nameIdentifier!!, "方法长度不能超过${MAX_LINES_PER_FUNCTION}行")
      } else {
        holder.registerWarning(function, "方法长度不能超过${MAX_LINES_PER_FUNCTION}行")
      }
    }
    val name = function.name
    if (name != null) {
      if (!Camel.test(name)) {
        registerNameError(function, Camel)
      }
      if (name.length == 1) {
        // 检查成员属性名称，不得少于2个字符
        holder.registerWarning(
            function.nameIdentifier ?: function,
            Messages.NAME_MUST_NOT_LESS_THAN2_CHARS,
            RenameIdentifierFix()
        )
      }
    }
  }

  override fun visitProperty(property: KtProperty, data: Any?) {
    if (property.hasSuppress(CommonCodeFormatVisitor.ALL)) {
      return
    }
    val name = property.name
    if (name != null) {
      val classOrObject = property.containingClassOrObject
      // const修饰的属性，顶层val属性，object的属性 使用常量命名规范
      if (property.hasModifier(CONST_KEYWORD)
          || (property.isVal && (property.isTopLevel
              || (property.isMember && classOrObject is KtObjectDeclaration)))) {
        if (classOrObject is KtObjectDeclaration && classOrObject.isCompanion()) {
          if (!Constant.test(name) && !Camel.test(name)) {
            registerNameError(property, Constant)
            registerNameError(property, Camel)
          }
        } else if (!Constant.test(name) && property.typeReference == null) {
          registerNameError(property, Constant)
        }
      } else {
        if (!Camel.test(name)) {
          registerNameError(property, Camel)
        }
      }
      if ((property.isMember || property.isTopLevel) && name.length == 1) {
        // 检查成员属性名称，不得少于2个字符
        holder.registerWarning(property.nameIdentifier ?: property, "成员属性名称不得少于两个字符", RenameIdentifierFix())
      }
    }
  }

  override fun visitReferenceExpression(expression: KtReferenceExpression, data: Any?) {
    // 使用日志输入代替System.out
    if (expression.text == "println" && (expression.ancestorOfType<KtFunction>() == null
            || !isInJUnitTestMethod(expression))) {
      holder.registerWarning(expression, "使用日志向控制台输出", KotlinConsolePrintFix())
    }
  }

  private fun isInJUnitTestMethod(expression: KtReferenceExpression): Boolean {
    return expression.getAncestorsOfType<KtFunction>().any { func ->
      func.annotationEntries.map { annotationEntry ->
        annotationEntry.toLightAnnotation()
      }.any { lightAnnotation ->
        lightAnnotation?.qualifiedName == JUNIT_TEST_ANNOTATION_CLASS_NAME
      }
    }
  }

  private fun registerNameError(element: KtCallableDeclaration, method: NamingMethod) {
    holder.registerWarning(element.nameIdentifier ?: element,
        "名称应该遵${method.zhName}命名法", KotlinNameFix(method))
  }

}
