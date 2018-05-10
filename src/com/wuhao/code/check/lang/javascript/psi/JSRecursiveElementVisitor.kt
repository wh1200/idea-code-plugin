/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.lang.javascript.psi

import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveVisitor

/**
 * javascript元素递归访问器
 * @author 吴昊
 * @since
 */
open class JSRecursiveElementVisitor : JSElementVisitor(), PsiRecursiveVisitor {

  override fun visitElement(element: PsiElement) {
    element.acceptChildren(this)
  }
}

