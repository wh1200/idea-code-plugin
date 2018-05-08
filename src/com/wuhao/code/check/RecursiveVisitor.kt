/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

/**
 * 递归访问psi元素
 * @author 吴昊
 * @since 1.2
 */
abstract class RecursiveVisitor : PsiElementVisitor() {

  abstract override fun visitElement(element: PsiElement)

  fun visit(element: PsiElement) {
    recursiveVisit(element)
  }

  private fun recursiveVisit(element: PsiElement) {
    visitElement(element)
    element.children.forEach {
      recursiveVisit(it)
    }
  }
}

