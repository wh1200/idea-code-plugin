package com.wuhao.code.check.inspection.fix.vue

import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.psi.PsiElement

/**
 * TODO
 *
 * @author 吴昊
 * @date 2021/4/8 6:33 上午
 * @since TODO
 * @version 1.0
 */

val PsiElement.jsDocComment: JSDocComment?
  get() {
    return if (this.firstChild is JSDocComment) {
      return this.firstChild as JSDocComment
    } else {
      null
    }
  }
