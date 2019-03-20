package com.wuhao.code.check.lang

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference

/**
 *
 * @author 吴昊
 * @since 1.4.6
 */
class VueTemplateReference(private val originEl: PsiElement,
                           private val refEl: PsiElement) : PsiReference {

  override fun bindToElement(el: PsiElement): PsiElement {
    throw IllegalStateException("can not bind")
  }

  override fun getCanonicalText(): String {
    return if (refEl is PsiNameIdentifierOwner) {
      refEl.name!!
    } else {
      refEl.text
    }
  }

  override fun getElement(): PsiElement {
    return originEl
  }

  override fun getRangeInElement(): TextRange {
    return element.textRange
  }

  override fun handleElementRename(newName: String): PsiElement {
    return if (refEl is PsiNameIdentifierOwner) {
      refEl.nameIdentifier!!
    } else {
      refEl
    }
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
