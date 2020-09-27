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

/**
 *
 * @author 吴昊
 * @since 1.0
 */
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
class ObjectFieldExpression(
    val key: String,
    val valueString: String?
) : VueCompositionExpresion() {

  override fun getText(): String {
    return "$key: ${valueString ?: "null"}"
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

  var initializer: String? = null
  var name = ""
  var type: String? = null

  override fun getText(): String {
    var text = "const $name"
    if (!type.isNullOrBlank()) {
      text += ": $type"
    }
    if (!initializer.isNullOrBlank()) {
      text += " = $initializer"
    }
    text += ";"
    return format(text)
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
    val lifeCycleExpressions: ArrayList<LifecycleExpression> = arrayListOf()
    val otherSetupExpressions = arrayListOf<String>()
    val vueImportSpecifics = arrayListOf<String>()

    /**  整个组件对象的属性 */
    val componentProps = arrayListOf<String>()
    val fields = arrayListOf<ES6FieldStatementImpl>()
    val functions = arrayListOf<TypeScriptFunction>()

    val propNames = arrayListOf<String>()
    val reactiveValueNames = arrayListOf<String>()
    val refValueNames = arrayListOf<String>()
    val injectionNames = arrayListOf<String>()
    val methodNames = arrayListOf<String>()
    val computedNames = arrayListOf<String>()
    cls.children.filter { it !is JSAttributeList }
        .forEach { field ->
          if (field is ES6FieldStatementImpl) {
            fields.add(field)
            val tsField = field.getChildOfType<TypeScriptField>()!!
            if (field.hasDecorator("Prop")) {
              propNames.add(tsField.name!!)
            } else {
              if (!field.attributeList!!.text.contains("static")
                  && !field.hasDecorator("Provide")) {
                if (field.hasDecorator("Inject")) {
                  injectionNames.add(tsField.name!!)
                } else {
                  if (tsField.initializer is JSObjectLiteralExpression) {
                    reactiveValueNames.add(tsField.name!!)
                  } else {
                    refValueNames.add(tsField.name!!)
                  }
                }
              }
            }
          } else if (field is TypeScriptFunction) {
            functions.add(field)
            if (!field.hasDecorator("Watch")) {
              // computed
              if (field.isGetProperty) {
                computedNames.add(field.name!!)
              } else {
                // methods
                if (field.name != "render" && field.name !in VUE2_LIFE_CYCLE_METHODS) {
                  methodNames.add(field.name!!)
                }
              }
            }
          }
        }
    fields.forEach { field ->
      val comment = field.docComment
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
        propsList.add(propExp)
      } else {
        val tsField = field.getChildByType<TypeScriptField>()!!

        val isStatic = field.attributeList!!.text.contains("static")
        if (isStatic) {
          componentProps.add(
              ObjectFieldExpression(tsField.name!!, tsField.initializer?.text)
                  .getText()
          )
        } else {
          val valueExp = ValueExpression()
          valueExp.comment = comment?.text ?: ""
          valueExp.name = tsField.name!!
          valueExp.type = tsField.typeElement?.text
          if (tsField.initializer != null) {
            val methodVisitor = VueMethodRecursiveVisitor(propNames,
                refValueNames, methodNames, computedNames, reactiveValueNames
            )
            methodVisitor.visitElement(tsField.initializer!!)
          }
          valueExp.initializer = tsField.initializer?.text
          if (field.hasDecorator("Provide")) {
            val provideName = field.attributeList!!.decorators[0].expression!!.getChildOfType<JSArgumentList>()!!.arguments[0]
            otherSetupExpressions.add("provide(${provideName.text}, ${tsField.initializer?.text})")
          } else if (field.hasDecorator("Inject")) {
            val arg = field.attributeList!!.decorators[0].expression!!.getChildOfType<JSArgumentList>()!!
                .arguments[0]
            if (arg is JSObjectLiteralExpression) {
              val injectName = arg.properties.find { it.name == "from" }!!.value!!.text
              val defaultStr = arg.properties.find { it.name == "default" }?.value?.text
              if (defaultStr.isNullOrBlank()) {
                valueExp.initializer = "inject(${injectName})"
              } else {
                valueExp.initializer = "inject(${injectName}, $defaultStr)"
              }
            } else {
              valueExp.initializer = "inject(${arg.text})"
            }
            valueList.add(valueExp)
          } else {
            // 不是prop
            if (tsField.initializer is JSObjectLiteralExpression) {
              valueExp.initializer = "reactive(${valueExp.initializer})"
            } else {
              if (!valueExp.type.isNullOrBlank()) {
                valueExp.type = "Ref<${valueExp.type}>"
              }
              if (valueExp.initializer.isNullOrBlank()) {
                valueExp.initializer = "ref(null)"
              } else {
                valueExp.initializer = "ref(${valueExp.initializer})"
              }
            }
            valueList.add(valueExp)
          }
        }
      }
    }
    functions.forEach { func ->
      val comment = func.docComment
      // watch
      if (func.hasDecorator("Watch")) {
        val decoratorArgs = (func.attributeList!!.decorators[0]
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
          VueMethodRecursiveVisitor(propNames,
              refValueNames, methodNames, computedNames, reactiveValueNames
          ).visitElement(valueArg!!)
          val watchProp = unwrap(valueArg?.text)
          if (watchProp in propNames) {
            we.watchExpression = "props.$watchProp"
          } else {
            we.watchExpression = "this.$watchProp"
          }
          val methodVisitor = VueMethodRecursiveVisitor(propNames,
              refValueNames, methodNames, computedNames, reactiveValueNames
          )
          methodVisitor.visitElement(func.block!!)
          we.callbackExpression = " ${func.parameterList?.text ?: "()"} => ${func.block?.text}"
          if (optionsArg != null) {
            we.optionsExpression = optionsArg.text
          }
        }
        watchList.add(we)
      } else {
        // computed
        if (func.isGetProperty) {
          val computed = ComputedExpression()
          computed.comment = comment?.text ?: ""
          computed.name = func.name ?: ""
          val methodVisitor = VueMethodRecursiveVisitor(propNames,
              refValueNames, methodNames, computedNames, reactiveValueNames
          )
          methodVisitor.visitElement(func.block!!)
          computed.expression = func.block?.text ?: "{}"
          computedList.add(computed)
        } else {
          // methods
          val fn = FunctionExpression()
          fn.comment = comment?.text ?: ""
          fn.name = func.name ?: ""
          fn.argumentsExpression = func.parameterList?.text ?: "()"
          // render
          if (fn.name == "render") {
            fn.bodyExpression = func.block?.text ?: "{}"
            renderFnString = if (comment != null) {
              "${comment.text}\nrender${fn.argumentsExpression} ${fn.bodyExpression}"
            } else {
              "render${fn.argumentsExpression} ${fn.bodyExpression}"
            }
          } else {
            val methodVisitor = VueMethodRecursiveVisitor(propNames,
                refValueNames, methodNames, computedNames, reactiveValueNames
            )
            methodVisitor.visitElement(func.block!!)
            if (methodVisitor.refs.isNotEmpty()) {
              methodVisitor.refs.forEach {
                if (valueList.none { it.name == "${it}Ref"})
                valueList.add(ValueExpression().apply {
                  name = "${it}Ref"
                  initializer = "ref(null)"
                })
              }
            }
            fn.bodyExpression = func.block?.text ?: "{}"
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
    componentProps.add(optionsDecoratorContent)
    componentProps.add("""props: {
${propsText.lines().joinToString("\n") { "      $it" }}
        }""")
    componentProps.add("""setup(props, {emit, slots}) {
$valueString
$watchString
$computedString
$functionString
$lifecycleString
${otherSetupExpressions.joinToString(";\n")}
          return {
          
          };
        }""")
    componentProps.add(renderFnString)
    val text = """
      defineComponent({
        ${componentProps.joinToString(",\n")}
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
    vueImportSpecifics.add("inject")
    vueImportSpecifics.add("provide")
    vueImportSpecifics.add("ref")
    vueImportSpecifics.add("Ref")
    vueImportSpecifics.add("reactive")
    vueImportSpecifics.add("nextTick")
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
