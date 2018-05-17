package com.wuhao.code.check.inspection.fix.java

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiIdentifier
import com.intellij.refactoring.rename.RenameUtil
import com.wuhao.code.check.toCamelCase

/**
 * 重命名Java元素（方法、字段、变量）为驼峰命名
 * @author 吴昊
 * @since
 */
class CamelCaseFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val identifier = descriptor.psiElement as PsiIdentifier
    val namedElement = identifier.parent
    val newName = identifier.text.toCamelCase()
    val usages = RenameUtil.findUsages(namedElement, newName, true, true, mapOf())
    RenameUtil.doRename(namedElement, newName, usages, project, null)
  }

  override fun getFamilyName(): String {
    return "重命名为Camel Case"
  }

}

