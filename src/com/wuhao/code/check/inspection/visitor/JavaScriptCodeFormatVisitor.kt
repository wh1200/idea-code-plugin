/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.impl.JSObjectLiteralExpressionImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.wuhao.code.check.inspection.fix.FileNameFix
import com.wuhao.code.check.inspection.fix.JsPropertySortFix

/**
 * Created by 吴昊 on 2018/4/28.
 */
open class JavaScriptCodeFormatVisitor(holder: ProblemsHolder) : BaseCodeFormatVisitor(holder) {

  override fun visitElement(element: PsiElement) {
    when (element) {
      is PsiFile -> checkFileName(element)
      is JSObjectLiteralExpressionImpl -> remindReorderProperties(element)
    }
  }

  private fun remindReorderProperties(element: JSObjectLiteralExpressionImpl) {
    val sortedProperties = element.properties.sortedBy { it.name }
    if (element.properties.toList() != sortedProperties) {
      holder.registerProblem(element, "对象属性排序", ProblemHighlightType.INFORMATION, JsPropertySortFix())
    }
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

  companion object {
    val JS_FILE_NAME_PATTERN = "^[a-z-_0-9]+.js\$".toRegex()
  }
}
