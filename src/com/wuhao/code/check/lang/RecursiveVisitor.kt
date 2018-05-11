/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.lang

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiRecursiveVisitor

/**
 * 递归访问psi元素
 * @author 吴昊
 * @since 1.2
 */
abstract class RecursiveVisitor : PsiElementVisitor(), PsiRecursiveVisitor {

  fun visit(element: PsiElement) {
    recursiveVisit(element)
  }

  abstract override fun visitElement(element: PsiElement)

  private fun recursiveVisit(element: PsiElement) {
    visitElement(element)
    element.children.forEach {
      recursiveVisit(it)
    }
  }
}

