/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement

import com.intellij.psi.PsiElement
import com.wuhao.code.check.RecursiveVisitor
import org.jetbrains.kotlin.psi.*

/**
 * kotlin元素递归访问器
 * @author 吴昊
 * @since
 */
abstract class KotlinRecursiveVisitor : RecursiveVisitor() {

  override fun visitElement(element: PsiElement) {
    if (element is KtClass) {
      visitClass(element)
    }
    if (element is KtProperty) {
      visitProperty(element)
    }
    if (element is KtNamedFunction) {
      visitFunction(element)
    }
    if (element is KtClassInitializer) {
      visitClassInitializer(element)
    }
    if (element is KtReferenceExpression) {
      visitReferenceExpression(element)
    }
  }

  open fun visitFunction(function: KtNamedFunction) {}

  open fun visitProperty(property: KtProperty) {}

  open fun visitClass(clazz: KtClass) {}

  open fun visitClassInitializer(initializer: KtClassInitializer) {

  }

  open fun visitReferenceExpression(expression: KtReferenceExpression){

  }
}

