/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.dialects.TypeScriptJSXLanguageDialect
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptObjectType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.wuhao.code.check.VUE_LANG_PATTERN
import com.wuhao.code.check.VUE_SCRIPT_TAG
import com.wuhao.code.check.constants.LanguageNames
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.constants.registerWarning
import com.wuhao.code.check.getAncestor
import com.wuhao.code.check.hasDecorator
import com.wuhao.code.check.inspection.fix.*
import com.wuhao.code.check.inspection.fix.vue.*

/**
 * Created by 吴昊 on 2018/4/28.
 *
 */
open class TypeScriptCodeFormatVisitor(val holder: ProblemsHolder) : JSElementVisitor(),
                                                                     BaseCodeFormatVisitor {

  companion object {
    val TS_FILE_NAME_PATTERN = "^[a-z-_0-9]+.ts(x)?\$".toRegex()
  }

  override fun support(language: Language): Boolean {
    return language.displayName == LanguageNames.TYPESCRIPT
        || language is TypeScriptJSXLanguageDialect
  }

  override fun visitFile(file: PsiFile) {
    if (!TS_FILE_NAME_PATTERN.matches(file.name) && !file.name.endsWith(".d.ts")
        && (file.name.endsWith(".ts") || file.name.endsWith(".tsx"))) {
      holder.registerWarning(file, Messages.JS_FILE_NAME_INVALID)
    }
    super.visitFile(file)
  }

  override fun visitES6ExportDefaultAssignment(node: ES6ExportDefaultAssignment?) {

    super.visitES6ExportDefaultAssignment(node)
  }

  override fun visitJSObjectLiteralExpression(element: JSObjectLiteralExpression) {
    val ac = when {
      element.parent is TypeScriptAsExpression -> element.getAncestor(4)
      else                                     -> element.getAncestor(3)
    }
    if ((element.findProperty("data") != null || element.findProperty("methods") != null)
        && element.findProperty("setup") == null) {
      holder.registerProblem(element, Messages.CONVERT_TO_VUE3_COMPONENT,
          ProblemHighlightType.INFORMATION,
          ConvertToVue3Component())
    }
    if (element.findProperty("name") != null) {
      holder.registerProblem(element, Messages.CONVERT_TO_CLASS_COMPONENT, ProblemHighlightType.INFORMATION,
          ConvertToClassComponent())
    }
    if (VUE_LANG_PATTERN.accepts(element)
        && VUE_SCRIPT_TAG.accepts(ac)) {
      val sortedProperties = VueComponentPropertySortFix.sortVueComponentProperties(element.properties)
      if (element.properties.toList() != sortedProperties) {
        holder.registerWarning(element, "Vue组件属性排序", VueComponentPropertySortFix())
      }
    } else {
      val sortedProperties = element.properties.sortedBy { it.name }
      if (element.properties.toList() != sortedProperties) {
        holder.registerProblem(element, "对象属性排序", ProblemHighlightType.INFORMATION, JsPropertySortFix())
      }
    }
    super.visitJSObjectLiteralExpression(element)
  }

  override fun visitTypeScriptClass(cls: TypeScriptClass) {
    if (cls.extendsList?.members?.any { it.referenceText == "React.Component" } == true) {
      holder.registerProblem(cls, "转为Vue组件",
          ProblemHighlightType.INFORMATION, ReactToVueFix())
    }
    if (cls.hasDecorator("Component")) {
      holder.registerProblem(cls, "转为Vue3组件",
          ProblemHighlightType.INFORMATION, Vue2ClassToVue3ClassFix())
      holder.registerProblem(cls, "转为Vue3 Composition API",
          ProblemHighlightType.INFORMATION, Vue2ClassToVue3CompositionAPIFix())
    }
    super.visitTypeScriptClass(cls)
  }

  override fun visitTypeScriptObjectType(objectType: TypeScriptObjectType?) {
    super.visitTypeScriptObjectType(objectType)
  }

}
