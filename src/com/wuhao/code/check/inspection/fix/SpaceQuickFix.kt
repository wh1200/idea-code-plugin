/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiWhiteSpace
import com.wuhao.code.check.Messages
import com.wuhao.code.check.insertElementAfter
import com.wuhao.code.check.insertElementBefore
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * 修复空格
 * @author 吴昊
 * @since 1.2.1
 */
class SpaceQuickFix(private val type: Type) : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val factory = KtPsiFactory(project)
    if (type == Type.BeforeParent) {
      element.parent.insertElementBefore(factory.createWhiteSpace(" "))
    }
    if (type in listOf(Type.Both, Type.After) && element.nextSibling !is PsiWhiteSpace) {
      element.insertElementAfter(factory.createWhiteSpace(" "))
    }
    if (type in listOf(Type.Both, Type.Before) && element.prevSibling !is PsiWhiteSpace) {
      element.insertElementBefore(factory.createWhiteSpace(" "))
    }
  }

  override fun getFamilyName(): String {
    return Messages.fixSpace
  }

  /**
   *
   * @author 吴昊
   * @since 1.2.1
   */
  enum class Type {
    After,

    AfterParent,

    Before,

    BeforeParent,

    Both
  }

}

