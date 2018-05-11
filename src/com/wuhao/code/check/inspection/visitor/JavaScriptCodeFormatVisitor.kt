/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.wuhao.code.check.LanguageNames
import com.wuhao.code.check.inspection.fix.FileNameFix
import com.wuhao.code.check.inspection.fix.JsPropertySortFix
import com.wuhao.code.check.lang.javascript.psi.JSRecursiveElementVisitor

/**
 * javascript文件代码格式检查访问器
 * 主要检查了javascript文件命名格式
 * Created by 吴昊 on 2018/4/28.
 * @author 吴昊
 * @since 1.1
 */
open class JavaScriptCodeFormatVisitor(val holder: ProblemsHolder) : JSRecursiveElementVisitor(), BaseCodeFormatVisitor {

  override fun support(language: Language): Boolean {
    return language == JavascriptLanguage.INSTANCE
        || language.displayName == LanguageNames.ecma6
  }

  override fun visitElement(element: PsiElement) {
  }

  override fun visitJSFile(file: JSFile) {
    checkFileName(file)
  }

  override fun visitJSObjectLiteralExpression(node: JSObjectLiteralExpression) {
    remindReorderProperties(node)
  }

  /**
   * 检查js文件名称的合法性，如果文件名称不合法，则进行提示，并在修复时弹出重命名对话框
   * @param element js文件对应的psi对象
   */
  private fun checkFileName(element: PsiFile) {
    if (!JS_FILE_NAME_PATTERN.matches(element.name)) {
      holder.registerProblem(element,
          "文件名称格式错误，只允许包含字母，数字，-及_",
          ProblemHighlightType.ERROR,
          FileNameFix())
    }
  }

  private fun remindReorderProperties(element: JSObjectLiteralExpression) {
    val sortedProperties = element.properties.sortedBy { it.name }
    if (element.properties.toList() != sortedProperties) {
      holder.registerProblem(element, "对象属性排序", ProblemHighlightType.INFORMATION, JsPropertySortFix())
    }
  }

  companion object {

    val JS_FILE_NAME_PATTERN = "^[a-z-_0-9]+.js\$".toRegex()

  }

}

