/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemHighlightType.ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.dialects.TypeScriptJSXLanguageDialect
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptObjectType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiFile
import com.intellij.psi.html.HtmlTag
import com.wuhao.code.check.constants.LanguageNames
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.constants.registerWarning
import com.wuhao.code.check.getAncestor
import com.wuhao.code.check.hasDecorator
import com.wuhao.code.check.inspection.fix.JsPropertySortFix
import com.wuhao.code.check.inspection.fix.SchemaFormFieldsTypeFix
import com.wuhao.code.check.inspection.fix.vue.ConvertToClassComponent
import com.wuhao.code.check.inspection.fix.vue.ReactToVueFix
import com.wuhao.code.check.inspection.fix.vue.Vue2ClassToVue3CompositionAPIFix
import org.jetbrains.kotlin.idea.completion.or
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage

/**
 * Created by 吴昊 on 2018/4/28.
 *
 */
open class TypeScriptCodeFormatVisitor(val holder: ProblemsHolder) : JSElementVisitor(),
    BaseCodeFormatVisitor {

  companion object {
    val TS_FILE_NAME_PATTERN = "^[a-z-_0-9]+.ts(x)?\$".toRegex()

    fun isRootVueComponentObject(element: JSObjectLiteralExpression): Boolean {
      val bc = element.getAncestor(2)
      val isVue3ComponentObject = bc is JSCallExpression && bc.methodExpression?.text in listOf(
          "defineComponent",
          "defineAsyncComponent"
      )
      if (isVue3ComponentObject) {
        return true
      }
      val ac = when (element.parent) {
        is TypeScriptAsExpression -> element.getAncestor(4)
        else                      -> element.getAncestor(3)
      }
      if (element.containingFile.language in listOf(VueLanguage.INSTANCE, VueJSLanguage.INSTANCE)) {
        return ac is HtmlTag
      }
      return false
    }
  }

  override fun support(language: Language): Boolean {
    return language.displayName == LanguageNames.TYPESCRIPT
        || language is TypeScriptJSXLanguageDialect
  }

  override fun visitES6ExportDefaultAssignment(node: ES6ExportDefaultAssignment?) {
    super.visitES6ExportDefaultAssignment(node)
  }

  override fun visitFile(file: PsiFile) {
    if (!TS_FILE_NAME_PATTERN.matches(file.name) && !file.name.endsWith(".d.ts")
        && (file.name.endsWith(".ts") || file.name.endsWith(".tsx"))
    ) {
      holder.registerWarning(file, Messages.JS_FILE_NAME_INVALID)
    }
    super.visitFile(file)
  }

  override fun visitJSObjectLiteralExpression(element: JSObjectLiteralExpression) {
    if (element.properties.isNotEmpty()) {
      if (element.findProperty("name") != null) {
        holder.registerProblem(
            element, Messages.CONVERT_TO_CLASS_COMPONENT, ProblemHighlightType.INFORMATION,
            ConvertToClassComponent()
        )
      }
      if (isSchemaFormObject(element)) {
        holder.registerProblem(element, "转为数组属性", ProblemHighlightType.INFORMATION, SchemaFormFieldsTypeFix())
      }
      if (!isRootVueComponentObject(element)) {
        val sortedProperties = element.properties.sortedBy { it.name }
        if (element.properties.isNotEmpty() && element.properties.toList() != sortedProperties) {
          holder.registerProblem(element, "对象属性排序", ProblemHighlightType.INFORMATION, JsPropertySortFix())
        }
      }
    }
    super.visitJSObjectLiteralExpression(element)
  }

  override fun visitTypeScriptClass(cls: TypeScriptClass) {
    if (cls.extendsList?.members?.any { it.referenceText == "React.Component" } == true) {
      holder.registerProblem(
          cls, "转为Vue组件",
          ProblemHighlightType.INFORMATION, ReactToVueFix()
      )
    } else if ((cls.hasDecorator("Component") || (
            cls.parent is ES6ExportDefaultAssignment && (cls.parent as ES6ExportDefaultAssignment).hasDecorator("Component")
            ))
        && cls.name != null && cls.name!!.matches(
            "[A-Z][a-z0-9]+".toRegex()
        )
    ) {
      holder.registerProblem(cls.nameIdentifier!!, "组件名称不能为单个单词", ERROR)
    }
    if (cls.hasDecorator("Component")) {
      holder.registerProblem(
          cls, "转为Vue3 Composition API",
          ProblemHighlightType.INFORMATION, Vue2ClassToVue3CompositionAPIFix()
      )
    }
    if (cls.parent is ES6ExportDefaultAssignment
        && (cls.parent as ES6ExportDefaultAssignment).hasDecorator("Component")
    ) {
      holder.registerProblem(
          cls, "转为Vue3 Composition API",
          ProblemHighlightType.INFORMATION, Vue2ClassToVue3CompositionAPIFix()
      )
    }
    super.visitTypeScriptClass(cls)
  }

  override fun visitTypeScriptObjectType(objectType: TypeScriptObjectType?) {
    super.visitTypeScriptObjectType(objectType)
  }


  private fun isSchemaFormObject(element: JSObjectLiteralExpression): Boolean {
    val parent = element.parent
    if (parent is TypeScriptAsExpression && parent.type?.text == "SchemaFormField") {
      return true
    }
    if (parent is TypeScriptVariable && parent.typeElement?.text == "SchemaFormField") {
      return true
    }
    return false
  }

}
