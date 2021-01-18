package com.wuhao.code.check.inspection.fix.vue

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl
import com.intellij.lang.javascript.TypeScriptJSXFileType
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.ES6ReferenceList
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory
import com.intellij.lang.typescript.psi.impl.ES6DecoratorImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.wuhao.code.check.getChildByType
import com.wuhao.code.check.hasDecorator
import com.wuhao.code.check.insertElementsBefore
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType

/**
 * TODO
 *
 * @author 吴昊
 * @date 2020/9/21 14:25
 * @since TODO
 * @version 1.0
 */
class Vue2ClassToVue3ClassFix : LocalQuickFix {

  override fun applyFix(project: Project, problem: ProblemDescriptor) {
    val cls = problem.psiElement as TypeScriptClass
    val decorator = cls.attributeList!!.decorators.find { it.decoratorName == "Component" }
        as ES6DecoratorImpl
    val argList = decorator.expression!!.getChildByType<JSArgumentList>()

    var bodyText = "class "
    var propsText = ""
    cls.children.filter { it !is JSAttributeList }
        .forEach { field ->
          if (field is ES6FieldStatementImpl && field.hasDecorator("Prop")) {
            field.children.forEach {
              if (it is JSAttributeList) {
                val fieldArgList = (it.decorators[0] as ES6Decorator).expression?.getChildOfType<JSArgumentList>()
                val propKey = field.getChildOfType<TypeScriptField>()?.name
                val propValue = fieldArgList?.text?.drop(1)?.dropLast(1)
                    ?: "{}"
                propsText += "$propKey: " + if (propValue.isBlank()) {
                  "{}"
                } else {
                  propValue
                } + ", \n"
                it.decorators[0].delete()
              }
            }

            bodyText += " ${field.text}\n"
          } else {
            if (field is ES6ReferenceList) {
              bodyText += " " + field.text + "{"
            } else {
              bodyText += " " + field.text
            }
          }
        }
    bodyText += "\n}"
    var optionsDecoratorContent = argList?.getChildOfType<JSObjectLiteralExpression>()?.text?.drop(1)?.dropLast(1)?.trim()
        ?: ""
    if (propsText.isNotEmpty()) {
      if (optionsDecoratorContent.isNotBlank()) {
        optionsDecoratorContent += ",\n"
      }
      optionsDecoratorContent += """props: {
        |  ${propsText.trim().dropLast(1)}
        |}
      """.trimMargin()
    }
    val text = """
      @Options({
        ${optionsDecoratorContent.trim()}
      })
      
      $bodyText
    """.trimIndent()
    val dummy = PsiFileFactory.getInstance(project).createFileFromText(
        "Dummy", TypeScriptJSXFileType.INSTANCE, text
    )
    val importStatement = JSPsiElementFactory.createJSSourceElement(
        "import {Options, Vue} from 'vue-class-component';", cls
    )
    cls.containingFile.getChildByType<ES6ImportDeclaration>()
    cls.containingFile.firstChild.insertElementsBefore(importStatement)
    cls.insertElementsBefore(*dummy.children)
    cls.delete()
  }

  override fun getFamilyName(): String {
    return "Vue"
  }

  override fun getName(): String {
    return "Vue2 Class 转 Vue3 Class"
  }

}
