package com.wuhao.code.check

import com.intellij.lang.javascript.psi.JSNewExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage

val NEW_VUE_PATTERN = psiElement(JSNewExpression::class.java).withChild(
    psiElement(JSReferenceExpression::class.java).withText("Vue")
)

val VUE_FILE = PlatformPatterns.psiFile().withLanguage(VueLanguage.INSTANCE)
    .andOr(PlatformPatterns.psiFile().withLanguage(VueJSLanguage.INSTANCE))

val VUE_LANG_PATTERN = psiElement()
    .inFile(VUE_FILE)

val VUE_SCRIPT_TAG = psiElement(XmlTag::class.java).withParent(VUE_LANG_PATTERN)
    .withName(HtmlUtil.SCRIPT_TAG_NAME)

fun leaf(): Capture<LeafPsiElement>? {
  return psiElement(LeafPsiElement::class.java)
}

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
