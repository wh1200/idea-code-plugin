/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl
import com.intellij.lang.javascript.TypeScriptJSXFileType
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.wuhao.code.check.firstChild
import com.wuhao.code.check.insertElementsBefore
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType

/**
 * javascript对象属性排序
 * @author 吴昊
 * @since 1.1
 */
class ReactToVueFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.psiElement as TypeScriptClass
    val typeArguments = el.extendsList!!.members[0].typeArguments
    val propsAttrList: MutableList<TypeScriptTypeMember> = mutableListOf()
    if (typeArguments.isNotEmpty()) {
      val props = typeArguments[0].firstChild.reference!!.resolve()
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
        .children.filter { it is ES6ImportDeclaration }.forEach { el ->
      if (file.children.filter { it is ES6ImportDeclaration }
              .none { it.text.trim() == el.text.trim() }) {
        file.addBefore(el, firstChildOfFile)
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
          else                                                  -> initProperty.value!!.text
        }
      }
      val decorator = if (defaultValueString != null) {
        "@Prop({default: $defaultValueString})"
      } else {
        "@Prop()"
      }
      "$decorator\npublic ${member.text};"
    }.joinToString("\n")
    println(propsString)
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
    }.joinToString("\n") { it.text }}
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
