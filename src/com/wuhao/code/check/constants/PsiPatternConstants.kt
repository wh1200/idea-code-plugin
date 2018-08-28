/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.constants

import com.intellij.psi.PsiElement
import com.intellij.psi.javadoc.PsiDocComment

/**
 * 判断是否有文档型注释
 */
fun PsiElement.hasDocComment(): Boolean {
  return this.firstChild is PsiDocComment
}

