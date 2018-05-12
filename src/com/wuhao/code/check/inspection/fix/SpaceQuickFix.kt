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
import com.wuhao.code.check.insertElementAfter
import com.wuhao.code.check.insertElementBefore
import com.wuhao.code.check.whiteSpace
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * 修复空格
 * @author 吴昊
 * @since 1.2.1
 */
class SpaceQuickFix(private val type: Type) : LocalQuickFix {

  /**
   * 自定义获取修复作用的元素，用于错误提示元素和修复作用的元素不是同一个的情况，
   * 比如错误提示注册在左大括号（{）,但实际修复时应当添加在块元素之前
   */
  private var getActionElement: (() -> PsiElement)? = null

  constructor(type: Type, getActionElement: () -> PsiElement) : this(type) {
    this.getActionElement = getActionElement
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val factory = KtPsiFactory(project)
    val actionElement = if (getActionElement?.invoke() != null) {
      getActionElement?.invoke()!!
    } else {
      element
    }
    when (type) {
      SpaceQuickFix.Type.After -> {
        insertSpaceAfter(actionElement)
      }
      SpaceQuickFix.Type.Before -> {
        insertSpaceBefore(actionElement)
      }
      SpaceQuickFix.Type.BeforeParent -> insertSpaceBefore(actionElement.parent)
      SpaceQuickFix.Type.Both -> {
        insertSpaceBefore(actionElement)
        insertSpaceAfter(actionElement)
      }
    }
  }

  override fun getFamilyName(): String {
    return Messages.fixSpace
  }

  private fun insertSpaceAfter(element: PsiElement) {
    if (element.nextSibling !is PsiWhiteSpace) {
      element.insertElementAfter(whiteSpace)
    } else {
      element.nextSibling.replace(whiteSpace)
    }
  }

  private fun insertSpaceBefore(element: PsiElement) {
    if (element.prevSibling !is PsiWhiteSpace) {
      element.insertElementBefore(whiteSpace)
    } else {
      element.prevSibling.replace(whiteSpace)
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

