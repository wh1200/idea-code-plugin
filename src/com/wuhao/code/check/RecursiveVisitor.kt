/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check

import com.intellij.psi.PsiElement

/**
 * 递归访问psi元素
 * @author 吴昊
 * @since 1.2
 */
abstract class RecursiveVisitor(private val element: PsiElement) {

  abstract fun visit(element: PsiElement)

  fun run() {
    recursiveVisit(element)
  }

  private fun recursiveVisit(element: PsiElement) {
    visit(element)
    element.children.forEach {
      recursiveVisit(it)
    }
  }
}
