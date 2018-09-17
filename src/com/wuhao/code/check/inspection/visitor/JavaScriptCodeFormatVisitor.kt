/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.constants.LanguageNames
import com.wuhao.code.check.constants.Messages.JS_FILE_NAME_INVALID
import com.wuhao.code.check.constants.registerError
import com.wuhao.code.check.getAncestor
import com.wuhao.code.check.inspection.fix.FileNameFix
import com.wuhao.code.check.inspection.fix.JsPropertySortFix
import com.wuhao.code.check.inspection.fix.VueComponentPropertySortFix
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor
import org.jetbrains.vuejs.VueLanguage

/**
 * javascript文件代码格式检查访问器
 * 主要检查了javascript文件命名格式
 * Created by 吴昊 on 2018/4/28.
 *
 * @author 吴昊
 * @since 1.1
 */
open class JavaScriptCodeFormatVisitor(val holder: ProblemsHolder) : JSElementVisitor(), BaseCodeFormatVisitor {

  companion object {
    val JS_FILE_NAME_PATTERN = "^[a-z-_0-9.]+.js\$".toRegex()
  }

  override fun support(language: Language): Boolean {
    return language == JavascriptLanguage.INSTANCE
        || language.displayName == LanguageNames.ECMA6
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
      holder.registerError(element,
          JS_FILE_NAME_INVALID, FileNameFix())
    }
  }

  private fun remindReorderProperties(element: JSObjectLiteralExpression) {
    val ac = element.getAncestor(3)
    if (element.containingFile.language is VueLanguage
        && ac is XmlTag && ac.name == VueArrangementVisitor.SCRIPT_TAG) {
      val sortedProperties = VueComponentPropertySortFix.sortVueComponentProperties(element.properties)
      if (element.properties.toList() != sortedProperties) {
        holder.registerError(element, "Vue组件属性排序", VueComponentPropertySortFix())
      }
    } else {
      val sortedProperties = element.properties.sortedBy { it.name }
      if (element.properties.toList() != sortedProperties) {
        holder.registerProblem(element, "对象属性排序", ProblemHighlightType.INFORMATION, JsPropertySortFix())
      }
    }
  }

}

