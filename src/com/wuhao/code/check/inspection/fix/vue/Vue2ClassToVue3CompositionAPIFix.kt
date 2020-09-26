package com.wuhao.code.check.inspection.fix.vue

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl
import com.intellij.lang.javascript.TypeScriptJSXFileType
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
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
import com.wuhao.code.check.constants.docComment
import com.wuhao.code.check.getChildByType
import com.wuhao.code.check.hasDecorator
import com.wuhao.code.check.insertElementsBefore
import com.wuhao.code.check.inspection.fix.vue.VueComponentPropertySortFix.Companion.VUE2_LIFE_CYCLE_METHODS
import com.wuhao.code.check.inspection.fix.vue.VueComponentPropertySortFix.Companion.VUE3_LIFE_CYCLE_METHOD_MAP
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

/**
 *
 * @author 吴昊
 * @since 1.0
 */
class ComputedExpression : VueCompositionExpresion() {

  var expression = ""
  var name = ""

  override fun getText(): String {
    return format("const $name = computed(() => $expression)")
  }

}

/**
 *
 * @author 吴昊
 * @since 1.0
 */
class FunctionExpression : VueCompositionExpresion() {

  var argumentsExpression = ""
  var bodyExpression = ""
  var name: String = ""

  override fun getText(): String {
    return format("const ${name} = ${argumentsExpression} => $bodyExpression")
  }

}

class LifecycleExpression : VueCompositionExpresion() {

  var bodyExpression: String = ""
  var name: String? = null

  override fun getText(): String {
    if (name.isNullOrBlank()) {
      return bodyExpression
    }
    return format("$name(() => $bodyExpression)")
  }

}

/**
 *
 * @author 吴昊
 * @since 1.0
 */
class PropExpression : VueCompositionExpresion() {

  var expression: String = "{}"
  var name: String = ""

  override fun getText(): String {
    return format("$name: $expression")
  }

}

/**
 *
 * @author 吴昊
 * @since 1.0
 */
class ValueExpression : VueCompositionExpresion() {

  var exp = ""

  override fun getText(): String {
    return format("const $exp;")
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
    val valueList: ArrayList<ValueExpression> = arrayListOf()
    val propsList: ArrayList<PropExpression> = arrayListOf()
    val watchList: ArrayList<WatchExpression> = arrayListOf()
    val functionList: ArrayList<FunctionExpression> = arrayListOf()
    val computedList: ArrayList<ComputedExpression> = arrayListOf()
    var renderFnString = ""
    val propsNames = arrayListOf<String>()
    val methodVisitor = VueMethodRecursiveVisitor(propsNames)
    val lifeCycleExpressions: ArrayList<LifecycleExpression> = arrayListOf()
    val vueImportSpecifics = arrayListOf<String>()
    cls.children.filter { it !is JSAttributeList }
        .forEach { field ->
          val comment = field.docComment
          if (field is ES6FieldStatementImpl) {
            if (field.hasDecorator("Prop")) {
              val attrList = field.attributeList
              val fieldArgList = (attrList!!.decorators[0] as ES6Decorator).expression?.getChildOfType<JSArgumentList>()
              val tsField = field.getChildOfType<TypeScriptField>()
              val propKey = tsField?.name
              val propExp = PropExpression()
              propExp.comment = comment?.text ?: ""
              propExp.name = propKey ?: ""
              if (fieldArgList != null && fieldArgList.arguments.size > 0) {
                val arg = fieldArgList.arguments[0];
                if (arg is JSObjectLiteralExpression) {
                  propExp.expression = """{
                    ${
                    arg.properties.joinToString(",\n") {
                      if (it.name == "type") {
                        it.text + " as PropType<${tsField!!.typeElement?.text}>"
                      } else {
                        it.text
                      }
                    }
                  }
}""".trimIndent()
                } else if (arg is JSArrayLiteralExpression || fieldArgList.arguments.size == 1) {
                  propExp.expression = """{
                    |type: ${arg.text} as PropType<${tsField!!.typeElement?.text}>
                    |}""".trimMargin()
                } else {
                  propExp.expression = """{
                    |type: [${fieldArgList.arguments.joinToString(", ") { it.text }}] as PropType<${
                    tsField!!
                        .typeElement?.text
                  }>
                    |}""".trimMargin()
                }
              }
              propsNames.add(propExp.name)
              propsList.add(propExp)
            } else {
              // 不是prop
              val valueExp = ValueExpression()
              valueExp.comment = comment?.text ?: ""
              valueExp.exp = field.getChildByType<TypeScriptField>()?.text ?: ""
              valueList.add(valueExp)

            }
          } else if (field is TypeScriptFunction) {
            // watch
            if (field.hasDecorator("Watch")) {
              val decoratorArgs = (field.attributeList!!.decorators[0]
                  .expression as JSCallExpression)
                  .argumentList
              val we = WatchExpression()
              we.comment = comment?.text ?: ""
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
                val watchProp = unwrap(valueArg?.text)
                if (watchProp in propsNames) {
                  we.watchExpression = "props.$watchProp"
                } else {
                  we.watchExpression = "this.$watchProp"
                }
                we.callbackExpression = " ${field.parameterList?.text ?: "()"} => ${field.block?.text}"
                if (optionsArg != null) {
                  we.optionsExpression = optionsArg.text
                }
              }
              watchList.add(we)
            } else {
              // computed
              if (field.isGetProperty) {
                val computed = ComputedExpression()
                computed.comment = comment?.text ?: ""
                computed.name = field.name ?: ""
                methodVisitor.visitElement(field.block!!)
                computed.expression = field.block?.text ?: "{}"
                computedList.add(computed)
              } else {
                // methods
                val fn = FunctionExpression()
                fn.comment = comment?.text ?: ""
                fn.name = field.name ?: ""
                fn.argumentsExpression = field.parameterList?.text ?: "()"
                // render
                if (fn.name == "render") {
                  fn.bodyExpression = field.block?.text ?: "{}"
                  if (comment != null) {
                    renderFnString = "${comment.text}\nrender${fn.argumentsExpression} ${fn.bodyExpression}"
                  } else {
                    renderFnString = "render${fn.argumentsExpression} ${fn.bodyExpression}"
                  }
                } else {
                  methodVisitor.visitElement(field.block!!)
                  fn.bodyExpression = field.block?.text ?: "{}"
                  val lifeCycleExpression = LifecycleExpression()
                  if (fn.name in VUE2_LIFE_CYCLE_METHODS) {
                    lifeCycleExpression.name = VUE3_LIFE_CYCLE_METHOD_MAP[fn.name]
                    if (!lifeCycleExpression.name.isNullOrBlank()) {
                      vueImportSpecifics.add(lifeCycleExpression.name!!)
                    }
                    lifeCycleExpression.comment = comment?.text ?: ""
                    lifeCycleExpression.bodyExpression = fn.bodyExpression
                    lifeCycleExpressions.add(lifeCycleExpression)
                  } else {
                    functionList.add(fn)
                  }
                }
              }
            }
          }
        }
    val optionsDecoratorContent = argList?.getChildOfType<JSObjectLiteralExpression>()?.text?.drop(1)?.dropLast(1)?.trim()
        ?: ""
    val propsText = propsList.map {
      it.getText()
    }.joinToString(",\n")
    val valueString = format(valueList)
    val watchString = format(watchList)
    val computedString = format(computedList)
    val functionString = format(functionList)
    val lifecycleString = format(lifeCycleExpressions)
    val componentProps = listOf<Pair<String, String>>()

    val text = """
      defineComponent({
        ${optionsDecoratorContent},
        props: {
${propsText.lines().joinToString("\n") { "      $it" }}
        },
        setup(props) {
$valueString
$watchString
$computedString
$functionString
$lifecycleString
          return {
          
          };
        },
        $renderFnString
      })
    """.trimIndent()
    val dummy = PsiFileFactory.getInstance(project).createFileFromText(
        "Dummy", TypeScriptJSXFileType.INSTANCE, text
    )
    if (watchList.isNotEmpty()) {
      vueImportSpecifics.add("watch")
    }
    if (computedList.isNotEmpty()) {
      vueImportSpecifics.add("computed")
    }
    vueImportSpecifics.addAll(listOf("PropType", "defineComponent"))
    val importStatement = JSPsiElementFactory.createJSSourceElement(
        "import {${vueImportSpecifics.joinToString(", ")}} from 'vue';", cls
    )
    cls.containingFile.firstChild.insertElementsBefore(importStatement)
    cls.insertElementsBefore(*dummy.children)
//    cls.delete()
  }

  override fun getFamilyName(): String {
    return "Vue2 Class 转 Vue3 Composition API"
  }

  private fun format(valueList: ArrayList<out VueCompositionExpresion>): String {
    return valueList.joinToString("\n") { it.getText() }.lines().joinToString("\n") { "      $it" }
  }

}

/**
 *
 * @author 吴昊
 * @since 1.0
 */
abstract class VueCompositionExpresion {

  var comment: String = ""

  fun format(text: String): String {
    if (comment.isNotBlank()) {
      return "${comment}\n$text"
    }
    return text;
  }

  abstract fun getText(): String

}

/**
 *
 * @author 吴昊
 * @since 1.0
 */
class WatchExpression : VueCompositionExpresion() {

  var callbackExpression: String = ""
  var optionsExpression: String = ""
  var watchExpression: String = ""

  override fun getText(): String {
    val text = if (optionsExpression.isNotBlank()) {
      "watch(() => ${watchExpression}, $callbackExpression, $optionsExpression)"
    } else {
      "watch(() => ${watchExpression}, $callbackExpression)"
    }
    return format(text)
  }

}
