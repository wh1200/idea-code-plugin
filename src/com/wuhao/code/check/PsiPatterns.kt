package com.wuhao.code.check

import com.intellij.lang.javascript.psi.JSNewExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.kotlin.idea.completion.or
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage

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

/**
 *
 * Created by 吴昊 on 2019/2/13.
 *
 * @author 吴昊
 * @since
 */
object PsiPatterns {

  val COMPONENT_DECORATOR_PATTERN = PlatformPatterns.psiElement(LeafPsiElement::class.java)
      .withText("Component").withAncestor(3, PlatformPatterns.psiElement(ES6Decorator::class.java))

  val NEW_VUE_PATTERN = psiElement(JSNewExpression::class.java).withChild(
      psiElement(JSReferenceExpression::class.java).withText("Vue")
  )

  val VUE_FILE = PlatformPatterns.psiFile().withLanguage(VueLanguage.INSTANCE)
      .or(PlatformPatterns.psiFile().withLanguage(VueJSLanguage.INSTANCE))

  val VUE_LANG_PATTERN = psiElement()
      .inFile(VUE_FILE)

  val VUE_SCRIPT_TAG = psiElement(XmlTag::class.java).withParent(VUE_LANG_PATTERN)
      .withName(HtmlUtil.SCRIPT_TAG_NAME)

}
