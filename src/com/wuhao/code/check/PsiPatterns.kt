package com.wuhao.code.check

import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

fun lengthGt(minLength: Int): Capture<PsiElement> {
  return psiElement().withTextLengthLongerThan(minLength)
}

fun textLengthLessOrEq(maxLength: Int): Capture<PsiElement> {
  return psiElement().andNot(lengthGt(maxLength))
}

fun PsiElement.typeMatch(type: IElementType): Boolean {
  return typePattern(type).accepts(this)
}

/**
 * 指定类型的匹配器
 */
fun typePattern(type: IElementType): Capture<PsiElement> {
  return psiElement().withElementType(type)
}
