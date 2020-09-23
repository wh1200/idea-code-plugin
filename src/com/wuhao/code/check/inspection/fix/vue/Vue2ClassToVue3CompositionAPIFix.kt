package com.wuhao.code.check.inspection.fix.vue

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl
import com.intellij.lang.javascript.TypeScriptJSXFileType
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
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
 * 去掉字符串前后的引号或括号
 * @param text
 * @return
 */
fun unwrap(text: String?): String? {
  if (text == null) {
    return null
  }
  return text.trim().drop(1).dropLast(1)
}

class ComputedExpression {

  var expression = ""
  var name = ""

  fun getText(): String {
    return "const $name = computed($expression)"
  }
}

class FunctionExpression {

  var argumentsExpression = ""
  var bodyExpression = ""
  var name: String = ""

  fun getText(): String {
    return "const ${name} = ${argumentsExpression} => $bodyExpression"
  }

}

class PropExpression {

  var expression: String = "{}"
  var name: String = ""

  fun getText(): String {
    return "$name: $expression"
  }

}

class ValueExpression {

  var exp = ""

  fun getText(): String {
    return "const $exp;"
  }

}

/**
 * TODO
 *
 * @author 吴昊
 * @date 2020/9/21 14:25
 * @since TODO
 * @version 1.0
 */
class Vue2ClassToVue3CompositionAPIFix : LocalQuickFix {

  override fun applyFix(project: Project, problem: ProblemDescriptor) {
    val cls = problem.psiElement as TypeScriptClass
    val decorator = cls.attributeList!!.decorators.find { it.decoratorName == "Component" }
        as ES6DecoratorImpl
    val argList = decorator.expression!!.getChildByType<JSArgumentList>()
    var valueList: ArrayList<ValueExpression> = arrayListOf()
    var bodyText = "class "
    val propsList: ArrayList<PropExpression> = arrayListOf()
    val watchList: ArrayList<WatchExpression> = arrayListOf()
    val functionList: ArrayList<FunctionExpression> = arrayListOf()
    val computedList: ArrayList<ComputedExpression> = arrayListOf()
    cls.children.filter { it !is JSAttributeList }
        .forEach { field ->
          if (field is ES6FieldStatementImpl) {
            if (field.hasDecorator("Prop")) {
              val attrList = field.attributeList
              val fieldArgList = (attrList!!.decorators[0] as ES6Decorator).expression?.getChildOfType<JSArgumentList>()
              val propKey = field.getChildOfType<TypeScriptField>()?.name
              val propValue = fieldArgList?.text?.drop(1)?.dropLast(1) ?: "{}"
              val propExp = PropExpression()
              propExp.name = propKey ?: ""
              val propValueExp = fieldArgList?.text?.drop(1)?.dropLast(1) ?: "{}"
              if (propValueExp.isNotBlank()) {
                propExp.expression = propValueExp
              }
              propsList.add(propExp)
            } else {
              // 不是prop
              val valueExp = ValueExpression()
              valueExp.exp = field.getChildByType<TypeScriptField>()?.text ?: ""
              valueList.add(valueExp)

            }
          } else {
            if (field is TypeScriptFunction) {
              // watch
              if (field.hasDecorator("Watch")) {
                val decoratorArgs = (field.attributeList!!.decorators[0]
                    .expression as JSCallExpression)
                    .argumentList
                val we = WatchExpression()
                if (decoratorArgs != null) {
                  val valueArg = if (decoratorArgs.arguments.isNotEmpty()) {
                    decoratorArgs.arguments[0]
                  } else {
                    null
                  }
                  val optionsArg = if (decoratorArgs.arguments.size > 1) {
                    decoratorArgs.arguments[1]
                  } else {
                    null
                  }
                  we.watchExpression = "() => ${unwrap(valueArg?.text)}"
                  we.callbackExpression = " () => ${field.block?.text}"
                  if (optionsArg != null) {
                    we.optionsExpression = optionsArg.text
                  }
                }
                watchList.add(we)
              } else {
                if (field.isGetProperty) {
                  val computed = ComputedExpression()
                  computed.name = field.name ?: ""
                  computed.expression = field.block?.text ?: "{}"
                  computedList.add(computed)
                } else {
                  val fn = FunctionExpression()
                  fn.name = field.name ?: ""
                  fn.argumentsExpression = field.parameterList?.text ?: "()"
                  fn.bodyExpression = field.block?.text ?: "{}"
                  functionList.add(fn)
                }
              }
            }
          }
        }
    bodyText += "\n}"
    var optionsDecoratorContent = argList?.getChildOfType<JSObjectLiteralExpression>()?.text?.drop(1)?.dropLast(1)?.trim()
        ?: ""
    val propsText = propsList.map {
      it.getText()
    }.joinToString(",\n")
    val text = """
      defineComponent({
        props: {
${propsText.lines().joinToString("\n") { "      $it" }}
        },
        setup(props) {
${valueList.joinToString("\n") { it.getText() }.lines().joinToString("\n") { "      $it" }}
${watchList.joinToString("\n") { it.getText() }.lines().joinToString("\n") { "      $it" }}
${computedList.joinToString("\n") {it.getText()}.lines().joinToString("\n") { "      $it" }}
          return {
          
          };
        }
      })
    """.trimIndent()
    val dummy = PsiFileFactory.getInstance(project).createFileFromText(
        "Dummy", TypeScriptJSXFileType.INSTANCE, text
    )
    val importStatement = JSPsiElementFactory.createJSSourceElement(
        "import {watch, computed, defineComponent} from 'vue';", cls
    )
    cls.containingFile.firstChild.insertElementsBefore(importStatement)
    cls.insertElementsBefore(*dummy.children)
//    cls.delete()
  }

  override fun getFamilyName(): String {
    return "Vue2 Class 转 Vue3 Composition API"
  }

}

class WatchExpression {

  var callbackExpression: String = ""
  var optionsExpression: String = ""
  var watchExpression: String = ""

  fun getText(): String {
    return "watch(${watchExpression}, $callbackExpression, $optionsExpression)"
  }

}
