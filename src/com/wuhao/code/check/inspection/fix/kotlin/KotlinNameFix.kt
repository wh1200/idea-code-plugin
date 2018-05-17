package com.wuhao.code.check.inspection.fix.kotlin

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.refactoring.rename.RenameUtil
import com.wuhao.code.check.*
import com.wuhao.code.check.enums.NamingMethod
import com.wuhao.code.check.enums.NamingMethod.*
import org.jetbrains.kotlin.psi.KtCallableDeclaration

/**
 * 重命名Kotlin元素（属性，方法）为驼峰命名
 * @author 吴昊
 * @since
 */
class KotlinNameFix(private val namingMethod: NamingMethod) : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val namedElement = descriptor.psiElement.parent as KtCallableDeclaration
    val newName = createNewName(namedElement.name!!)
    val usages = RenameUtil.findUsages(namedElement, newName, true, true, mapOf())
    RenameUtil.doRename(namedElement, newName, usages, project, null)
  }

  override fun getFamilyName(): String {
    return "使用${namingMethod.zhName}命名法重命名"
  }

  private fun createNewName(name: String): String {
    return when (namingMethod) {
      Camel -> name.toCamelCase()
      Pascal -> name.toPascalCase()
      Constant -> name.toConstantCase()
      Dash -> name.toDashCase()
      Underline -> name.toUnderlineCase()
    }
  }

}

