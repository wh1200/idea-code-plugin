package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.core.replaced

/**
 *
 * Created by 吴昊 on 2019/2/11.
 *
 * @author 吴昊
 * @since 1.4.6
 */
class ReplaceWithElementFix(val fn: () -> PsiElement) : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.psiElement
    el.replaced(fn())
  }

  override fun getFamilyName(): String {
    return "修复"
  }

}
