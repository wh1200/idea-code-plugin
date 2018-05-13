/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check

import com.intellij.psi.PsiElement

/**
 *
 * @author 吴昊
 * @since
 */
abstract class MyRecursiveElementVisitor {

  fun visit(element: PsiElement) {
    visitElement(element)
    element.children.forEach { child ->
      this.visit(child)
    }
  }

  abstract fun visitElement(element: PsiElement)

}

