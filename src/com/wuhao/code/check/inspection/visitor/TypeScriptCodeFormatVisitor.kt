/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.constants.LanguageNames
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.constants.registerError
import com.wuhao.code.check.getAncestor
import com.wuhao.code.check.inspection.fix.ConvertToClassComponent
import com.wuhao.code.check.inspection.fix.JsPropertySortFix
import com.wuhao.code.check.inspection.fix.VueComponentPropertySortFix
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.SCRIPT_TAG
import org.jetbrains.vuejs.VueLanguage

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


  override fun visitFile(file: PsiFile) {
    if (!TS_FILE_NAME_PATTERN.matches(file.name) && !file.name.endsWith(".d.ts")) {
      holder.registerError(file, Messages.JS_FILE_NAME_INVALID)
    }
    super.visitFile(file)
  }


  override fun visitJSObjectLiteralExpression(element: JSObjectLiteralExpression) {
    val ac = if (element.parent is TypeScriptAsExpression) {
      element.getAncestor(4)
    } else {
      element.getAncestor(3)
    }
    if (element.containingFile.language is VueLanguage
        && ac is XmlTag && ac.name == SCRIPT_TAG) {
      val sortedProperties = VueComponentPropertySortFix.sortVueComponentProperties(element.properties)
      if (element.properties.toList() != sortedProperties) {
        holder.registerError(element, "Vue组件属性排序", VueComponentPropertySortFix())
      }
      holder.registerProblem(element, Messages.CONVERT_TO_CLASS_COMPONENT, ProblemHighlightType.INFORMATION, ConvertToClassComponent())
    } else {
      val sortedProperties = element.properties.sortedBy { it.name }
      if (element.properties.toList() != sortedProperties) {
        holder.registerProblem(element, "对象属性排序", ProblemHighlightType.INFORMATION, JsPropertySortFix())
      }
    }
    super.visitJSObjectLiteralExpression(element)
  }

}

