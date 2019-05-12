/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.TypeScriptJSXFileType
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptPropertySignatureImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.wuhao.code.check.firstChild
import com.wuhao.code.check.insertElementsBefore
import com.wuhao.code.check.typeMatch
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType

/**
 *
 * @author 吴昊
 * @since 1.4.8
 */
class JsRecursiveVisitor : JSElementVisitor() {

  val propsExpressions = arrayListOf<JSReferenceExpression>()
  val stateCallExpressions = arrayListOf<JSCallExpression>()

  override fun visitElement(element: PsiElement?) {
    super.visitElement(element)
    element?.children?.forEach {
      it.accept(this)
    }
  }

  override fun visitJSCallExpression(node: JSCallExpression) {
    if (node.firstChild is JSReferenceExpression
        && node.firstChild.text == "this.setState") {
      stateCallExpressions.add(node)
    }
  }

  override fun visitJSReferenceExpression(node: JSReferenceExpression) {
    if (node.text == "this.props") {
      propsExpressions.add(node)
    } else {
      super.visitJSReferenceExpression(node)
    }
  }

}

class ObjectDescriptor {

  private val properties: ArrayList<Property> = arrayListOf()

  fun addProperty(name: String, value: String) {
    properties.add(Property(name, value))
  }

  override fun toString(): String {
    return """{${properties.joinToString(",\n")}}"""
  }

  class Property(val name: String, val value: String) {

    override fun toString(): String {
      return "$name: $value"
    }

  }

}

/**
 * javascript对象属性排序
 * @author 吴昊
 * @since 1.1
 */
class ReactToVueFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val visitor = JsRecursiveVisitor()
    val el = descriptor.psiElement as TypeScriptClass
    val typeArguments = el.extendsList!!.members[0].typeArguments
    val propsAttrList: MutableList<TypeScriptTypeMember> = mutableListOf()
    el.accept(visitor)
    visitor.stateCallExpressions.forEach { node ->
      // 将react的setState调用的第一个参数对象转化为赋值表达式
      if (node.arguments.size == 1 && node.arguments[0] is JSObjectLiteralExpression) {
        val properties = (node.arguments[0] as JSObjectLiteralExpression).properties
        val dummy = PsiFileFactory.getInstance(project).createFileFromText(
            "Dummy", TypeScriptJSXFileType.INSTANCE, properties.map {
          "this.${it.name} = ${it.value!!.text};"
        }.joinToString("\n"))
        node.insertElementsBefore(*dummy.children)
        // 删除;
        if (node.nextSibling.typeMatch(JSTokenTypes.SEMICOLON)) {
          node.nextSibling.delete()
        }
        // 删除setState语句
        node.delete()
      }
    }
    visitor.propsExpressions.forEach { node ->
      node.children.forEach {
        if (it !is JSThisExpression) {
          it.delete()
        }
      }
    }
    if (typeArguments.isNotEmpty()) {
      val props = typeArguments[0].firstChild.reference?.resolve()
      if (props is TypeScriptInterface) {
        propsAttrList.addAll(props.body.typeMembers)
        if (props.extendsList != null) {
          val ref = props.extendsList!!.members[0].children[0].reference?.resolve()
          if (ref is TypeScriptInterface) {
            propsAttrList.addAll(ref.body.typeMembers)
          }
        }
      }
    }
    val file = el.containingFile as JSFile
    val firstChildOfFile = file.firstChild
    PsiFileFactory.getInstance(project)
        .createFileFromText(TypeScriptLanguageDialect.findLanguageByID("TypeScript JSX")!!,
            """import Vue from 'vue';
              |import {Prop} from 'vue-property-decorator';
              |import Component from 'vue-class-component';""".trimMargin())
        .children.filter { it is ES6ImportDeclaration }.forEach { importEl ->
      if (file.children.filter { it is ES6ImportDeclaration }
              .none { it.text.trim() == importEl.text.trim() }) {
        file.addBefore(importEl, firstChildOfFile)
      }
    }
    var initProperties: List<JSProperty> = listOf()
    val defaultPropsField = el.children.filter {
      it is ES6FieldStatementImpl
    }.map { it as ES6FieldStatementImpl }.map { it.firstChild { it is TypeScriptField } }
        .firstOrNull { (it as TypeScriptField).name == "defaultProps" }
    if (defaultPropsField != null) {
      val init = (defaultPropsField as TypeScriptField).initializer
      if (init is JSObjectLiteralExpression) {
        initProperties = init.properties.toList()
      }
    }
    val propsString = propsAttrList.map { member ->
      val initProperty = initProperties.firstOrNull { it.name == member.name }
      var defaultValueString: String? = null
      if (initProperty != null) {
        defaultValueString = when {
          initProperty.value is JSFunctionExpression
              || initProperty.value is JSObjectLiteralExpression
              || initProperty.value is JSArrayLiteralExpression -> """() => { return ${initProperty.value!!.text};}"""
          else                                                  -> initProperty.value?.text
        }
      }
      var typeString: String? = null
      if (member is TypeScriptPropertySignatureImpl) {
        typeString = when (member.type?.typeText) {
          "number"  -> "Number"
          "boolean" -> "Boolean"
          "string"  -> "String"
          else      -> null
        }
      }
      val objectDescriptor = ObjectDescriptor()
      if (typeString != null) {
        objectDescriptor.addProperty("type", typeString)
      }
      if (defaultValueString != null) {
        objectDescriptor.addProperty("default", defaultValueString)
      }
      val decorator = "@Prop($objectDescriptor)"
      "$decorator\npublic ${member.text};"
    }.joinToString("\n")
    val text = """
      |@Component({
      |  name: ''
      |})
      |${el.attributeList?.text}
      |class ${el.name} extends Vue {
      | $propsString
      | ${el.children.filter {
      (it is ES6FieldStatementImpl && it.getChildOfType<TypeScriptField>()?.name != "defaultProps")
          || it is TypeScriptFunction
    }.joinToString("\n") { "public " + it.text }}
      |}""".trimMargin()
    val dummy = PsiFileFactory.getInstance(project).createFileFromText(
        "Dummy", TypeScriptJSXFileType.INSTANCE, text)
    el.insertElementsBefore(*dummy.children)
    el.delete()
  }

  override fun getFamilyName(): String {
    return "React组件转Vue组件"
  }

}
