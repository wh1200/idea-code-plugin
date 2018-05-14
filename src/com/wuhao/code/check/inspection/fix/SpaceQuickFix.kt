/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.wuhao.code.check.Messages
import com.wuhao.code.check.getWhiteSpace
import com.wuhao.code.check.insertElementAfter
import com.wuhao.code.check.insertElementBefore

/**
 * 修复空格
 * @author 吴昊
 * @since 1.2.1
 */
class SpaceQuickFix(private val type: Type) : LocalQuickFix {



  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    when (type) {
      SpaceQuickFix.Type.After -> {
        insertSpaceAfter(element)
      }
      SpaceQuickFix.Type.Before -> {
        insertSpaceBefore(element)
      }
      SpaceQuickFix.Type.BeforeParent -> insertSpaceBefore(element.parent)
      SpaceQuickFix.Type.Both -> {
        insertSpaceBefore(element)
        insertSpaceAfter(element)
      }
    }
  }

  override fun getFamilyName(): String {
    return Messages.fixSpace
  }

  private fun insertSpaceAfter(element: PsiElement) {
    if (element.nextSibling !is PsiWhiteSpace) {
      element.insertElementAfter(getWhiteSpace(element.project))
    } else {
      element.nextSibling.replace(getWhiteSpace(element.project))
    }
  }

  private fun insertSpaceBefore(element: PsiElement) {
    if (element.prevSibling !is PsiWhiteSpace) {
      element.insertElementBefore(getWhiteSpace(element.project))
    } else {
      element.prevSibling.replace(getWhiteSpace(element.project))
    }
  }

  /**
   *
   * @author 吴昊
   * @since 1.2.1
   */
  enum class Type {

    After,
    Before,
    BeforeParent,
    Both;

  }

}

