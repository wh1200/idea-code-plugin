package com.wuhao.code.check

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
import org.jetbrains.vuejs.VueLanguage
import org.jetbrains.vuejs.language.VueJSLanguage

fun leaf(): Capture<LeafPsiElement>? {
  return psiElement(LeafPsiElement::class.java)
}

fun lengthGt(minLength: Int): Capture<PsiElement> {
  return psiElement().withTextLengthLongerThan(minLength)
}

fun textLengthLessOrEq(maxLength: Int): Capture<PsiElement> {
  return psiElement().andNot(lengthGt(maxLength))
}

/**
 * 指定类型的匹配器
 */
fun typePattern(type: IElementType): Capture<PsiElement> {
  return psiElement().withElementType(type)
}

fun PsiElement.typeMatch(type:IElementType):Boolean {
  return typePattern(type).accepts(this)
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

  val VUE_FILE = PlatformPatterns.psiFile().withLanguage(VueLanguage.INSTANCE)
      .or(PlatformPatterns.psiFile().withLanguage(VueJSLanguage.INSTANCE))

  val VUE_LANG_PATTERN = PlatformPatterns.psiElement()
      .inFile(VUE_FILE)

  val VUE_SCRIPT_TAG = PlatformPatterns.psiElement(XmlTag::class.java).withParent(VUE_LANG_PATTERN)
      .withName(HtmlUtil.SCRIPT_TAG_NAME)

}