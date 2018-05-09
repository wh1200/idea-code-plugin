/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement.kotlin

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveVisitor
import com.intellij.util.containers.Stack
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtVisitor

/**
 * kotlin元素递归访问器
 * @author 吴昊
 * @since
 */
abstract class KotlinRecursiveVisitor : KtVisitor<Any, Any>(), PsiRecursiveVisitor {

  private val myRefExprsInVisit = Stack<KtReferenceExpression>()

  override fun visitElement(element: PsiElement) {
    if (!myRefExprsInVisit.isEmpty() && myRefExprsInVisit.peek() === element) {
      myRefExprsInVisit.pop()
      myRefExprsInVisit.push(null)
    } else {
      element.acceptChildren(this)
    }
  }

  override fun visitReferenceExpression(expression: KtReferenceExpression, data: Any?): Any? {
    myRefExprsInVisit.push(expression)
    try {
      visitExpression(expression, data)
    } finally {
      myRefExprsInVisit.pop()
    }
    return super.visitReferenceExpression(expression, data)
  }
}
