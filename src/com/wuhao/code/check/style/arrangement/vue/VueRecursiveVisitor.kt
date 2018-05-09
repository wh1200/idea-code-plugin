/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement.vue

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveVisitor
import org.jetbrains.vuejs.codeInsight.VueFileVisitor

/**
 * vue文件psi元素递归访问器
 * @author 吴昊
 * @since
 */
open class VueRecursiveVisitor : VueFileVisitor(), PsiRecursiveVisitor {

  override fun visitElement(element: PsiElement) {
    element.acceptChildren(this)
  }


}

