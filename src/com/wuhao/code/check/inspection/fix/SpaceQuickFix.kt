/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.createJsWhiteSpace
import com.wuhao.code.check.insertElementAfter
import com.wuhao.code.check.insertElementBefore

/**
 * 修复空格
 * @author 吴昊
 * @since 1.2.1
 */
class SpaceQuickFix(private val type: Position) : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    when (type) {
      SpaceQuickFix.Position.After -> {
        insertSpaceAfter(element)
      }
      SpaceQuickFix.Position.Before -> {
        insertSpaceBefore(element)
      }
      SpaceQuickFix.Position.BeforeParent -> insertSpaceBefore(element.parent)
      SpaceQuickFix.Position.Both -> {
        insertSpaceBefore(element)
        insertSpaceAfter(element)
      }
    }
  }

  override fun getFamilyName(): String {
    return Messages.FIX_SPACE
  }

  private fun insertSpaceAfter(element: PsiElement) {
    if (element.nextSibling !is PsiWhiteSpace) {
      element.insertElementAfter(element.createJsWhiteSpace())
    } else {
      element.nextSibling.replace(element.createJsWhiteSpace())
    }
  }

  private fun insertSpaceBefore(element: PsiElement) {
    if (element.prevSibling !is PsiWhiteSpace) {
      element.insertElementBefore(element.createJsWhiteSpace())
    } else {
      element.prevSibling.replace(element.createJsWhiteSpace())
    }
  }

  /**
   * 对psi元素做操作时，操作作用的位置（相对于psi元素）
   * @author 吴昊
   * @since 1.2.1
   */
  enum class Position {
    /**
     * psi元素之后
     */
    After,
    /**
     * psi元素之前
     */
    Before,
    /**
     * psi元素的父元素之前
     */
    BeforeParent,
    /**
     * psi元素之前和之后
     */
    Both;

  }

}

