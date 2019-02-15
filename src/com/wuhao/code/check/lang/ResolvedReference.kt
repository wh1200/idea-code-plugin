package com.wuhao.code.check.lang

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

/**
 *
 * @author 吴昊
 * @since 1.4.6
 */
class ResolvedReference(private val originEl: PsiElement, private val refEl: PsiElement,
                        private val name: String, private val nameElement: PsiElement) : PsiReference {

  override fun bindToElement(el: PsiElement): PsiElement {
    throw IllegalStateException("can not bind")
  }

  override fun getCanonicalText(): String {
    return name
  }

  override fun getElement(): PsiElement {
    return originEl
  }

  override fun getRangeInElement(): TextRange {
    return element.textRange
  }

  override fun handleElementRename(newName: String): PsiElement {
    return nameElement
  }

  override fun isReferenceTo(p0: PsiElement): Boolean {
    return false
  }

  override fun isSoft(): Boolean {
    return false
  }

  override fun resolve(): PsiElement? {
    return refEl
  }

}
