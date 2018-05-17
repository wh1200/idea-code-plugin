package com.wuhao.code.check.inspection.fix.kotlin

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.refactoring.rename.RenameUtil
import com.wuhao.code.check.enums.NamingMethod
import com.wuhao.code.check.inspection.fix.BaseNameFix
import org.jetbrains.kotlin.psi.KtCallableDeclaration

/**
 * 重命名Kotlin元素（属性，方法）为驼峰命名
 * @author 吴昊
 * @since
 */
class KotlinNameFix(namingMethod: NamingMethod) : BaseNameFix(namingMethod) {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val namedElement = descriptor.psiElement.parent as KtCallableDeclaration
    val newName = getNewName(namedElement.name!!)
    val usages = RenameUtil.findUsages(namedElement, newName, true, true, mapOf())
    RenameUtil.doRename(namedElement, newName, usages, project, null)
  }

}

