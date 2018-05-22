/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.impl.JSObjectLiteralExpressionImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.wuhao.code.check.constants.LanguageNames
import com.wuhao.code.check.inspection.fix.JsPropertySortFix
import com.wuhao.code.check.constants.registerError

/**
 * Created by 吴昊 on 2018/4/28.
 *
 */
open class TypeScriptCodeFormatVisitor(val holder: ProblemsHolder) : JSElementVisitor(),
    BaseCodeFormatVisitor {

  companion object {
    val TS_FILE_NAME_PATTERN = "^[a-z-_0-9]+.ts\$".toRegex()
  }

  override fun support(language: Language): Boolean {
    return language.displayName == LanguageNames.TYPESCRIPT
  }

  override fun visitElement(element: PsiElement) {
    when (element) {
      is PsiFile -> {
        if (!TS_FILE_NAME_PATTERN.matches(element.name)) {
          holder.registerError(element, "文件名称格式错误，只允许包含字母，数字，-及_")
        }
      }
      is JSObjectLiteralExpressionImpl -> {
        val sortedProperties = element.properties.sortedBy { it.name }
        if (element.properties.toList() != sortedProperties) {
          holder.registerProblem(element, "对象属性排序", ProblemHighlightType.INFORMATION, JsPropertySortFix())
        }
      }
    }
  }

}

