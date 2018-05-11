/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement.kotlin

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveVisitor
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.psi.KtVisitor

/**
 * kotlin元素递归访问器
 * @author 吴昊
 * @since
 */
abstract class KotlinRecursiveVisitor : KtVisitor<Any, Any>(), PsiRecursiveVisitor {

  open fun visitDoc(doc: KDoc) {
  }

  open fun visitDocSection(section: KDocSection) {
  }

  override fun visitElement(element: PsiElement) {
    when (element) {
      is KDoc -> visitDoc(element)
      is KDocSection -> visitDocSection(element)
    }
    element.acceptChildren(this)
  }

}

