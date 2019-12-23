/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.dialects.ECMA6LanguageDialect
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.xml.XMLLanguage
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.wuhao.code.check.constants.registerWarning
import com.wuhao.code.check.vueEnabled
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage
import java.nio.charset.StandardCharsets

/**
 * 基本的代码格式检查访问器，主要检查了文件缩进及文件编码
 * 文件缩进强制为2个空格，持续缩进为4个空格，文件编码为UTF-8
 *
 * Created by 吴昊 on 18-4-26.
 * @author 吴昊
 * @since 1.1
 */
class CommonCodeFormatVisitor(private val holder: ProblemsHolder) : PsiElementVisitor(), BaseCodeFormatVisitor {

  companion object {
    const val ACTION_PREFIX = "@"
    const val ALL = "ALL"
    const val API_MODEL_PROPERTY = "io.swagger.annotations.ApiModelProperty"
    const val CUSTOM_ATTR_PREFIX = ":"
    const val DIRECTIVE_PREFIX = "v-"
  }

  override fun support(language: Language): Boolean {
    if (vueEnabled && (language is VueLanguage || language is VueJSLanguage)) {
      return true
    }
    return language is KotlinLanguage
        || language is JavaLanguage
        || language is JavascriptLanguage
        || language is TypeScriptLanguageDialect
        || language is ECMA6LanguageDialect
        || language is XMLLanguage
  }

  override fun visitFile(file: PsiFile) {
    this.checkEncoding(file)
  }

  private fun checkEncoding(element: PsiElement) {
    if (element is PsiFile && element.virtualFile != null && element.virtualFile.charset != StandardCharsets.UTF_8) {
      holder.registerWarning(element, "${element.name}的编码为${element.virtualFile.charset}，应该使用UTF-8")
    }
  }

}
