/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.refactoring.rename.RenamePsiFileProcessor
import org.jetbrains.kotlin.idea.inspections.findExistingEditor

/**
 * javascript文件名修复
 * @author 吴昊
 * @since 1.1
 */
class FileNameFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    RenamePsiFileProcessor.PsiFileRenameDialog(
        project,
        descriptor.psiElement,
        null,
        descriptor.psiElement.findExistingEditor()).show()
  }

  override fun getFamilyName(): String {
    return "修复文件名"
  }

}

