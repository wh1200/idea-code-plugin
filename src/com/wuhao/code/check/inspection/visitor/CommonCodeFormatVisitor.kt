/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.application.options.CodeStyle
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.dialects.ECMA6LanguageDialect
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.JavaCodeStyleSettings
import com.wuhao.code.check.DEFAULT_CONTINUATION_INDENT_SPACE_COUNT
import com.wuhao.code.check.DEFAULT_INDENT_SPACE_COUNT
import com.wuhao.code.check.PluginStart
import com.wuhao.code.check.registerError
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.vuejs.VueLanguage
import org.jetbrains.vuejs.language.VueJSLanguage
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
    const val CUSTOM_ATTR_PREFIX = ":"
    const val DIRECTIVE_PREFIX = "v-"
  }

  override fun support(language: Language): Boolean {
    return language is KotlinLanguage ||
        language is JavaLanguage
        || language is JavascriptLanguage
        || language is TypeScriptLanguageDialect
        || language is ECMA6LanguageDialect
        || language is VueLanguage
        || language is XMLLanguage
        || language is VueJSLanguage
  }

  override fun visitFile(file: PsiFile) {
    this.checkEncoding(file)
    this.checkIndent(file)
  }

  private fun checkEncoding(element: PsiElement) {
    if (element is PsiFile && element.virtualFile != null && element.virtualFile.charset != StandardCharsets.UTF_8) {
      holder.registerError(element, "${element.name}的编码为${element.virtualFile.charset}，应该使用UTF-8")
    }
  }

  private fun checkIndent(element: PsiElement) {
    if (element is PsiFile) {
      val styleContainer = JavaCodeStyleSettings.getInstance(element.project)
          .container
      val indent = styleContainer.getIndentSize(element.fileType)
      val continuationIndent = styleContainer.getContinuationIndentSize(element.fileType)
      if (indent != DEFAULT_INDENT_SPACE_COUNT) {
        val indentFix = object : LocalQuickFix {

          override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            PluginStart.setIndent(element.fileType, element.language, CodeStyle.getSettings(element.project))
          }

          override fun getFamilyName(): String {
            return "设置缩进"
          }

        }
        holder.registerError(element, "${element.fileType.name}文件的缩进必须为${DEFAULT_INDENT_SPACE_COUNT}个空格",
            indentFix)
      }
      if (continuationIndent != DEFAULT_CONTINUATION_INDENT_SPACE_COUNT) {
        holder.registerError(element, "${element.fileType.name}文件的持续缩进必须为${DEFAULT_CONTINUATION_INDENT_SPACE_COUNT}个空格")
      }
    }
  }

}

