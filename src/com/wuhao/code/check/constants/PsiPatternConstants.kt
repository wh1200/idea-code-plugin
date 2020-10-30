/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.constants

import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiJavaToken
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.java.PsiAnnotationImpl
import com.intellij.psi.javadoc.PsiDocComment
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtConstructorCalleeExpression

val PsiElement.docComment: PsiComment?
  get() {
    if (this.hasDocComment()) {
      val doc = this.firstChild
      if (doc is KDoc) {
        return doc
      }
      if (doc is PsiDocComment) {
        return doc
      }
      if (doc is JSDocComment) {
        return doc
      }
    }
    return null
  }
private const val VALUE_ANNOTATION_NAME = "Value"
/**  匹配java文件中的@Value注解中的字符串元素 */
val JAVA_VALUE_ANNOTATION_PATTERN: PsiElementPattern.Capture<PsiJavaToken> =
    psiElement(PsiJavaToken::class.java)
        .withSuperParent(4, psiElement(PsiAnnotationImpl::class.java)
            .withChild(psiElement(PsiJavaCodeReferenceElement::class.java)
                .withText(VALUE_ANNOTATION_NAME)))
/**  匹配kt文件中的@Value注解中的字符串元素 */
val KOTLIN_VALUE_ANNOTATION_PATTERN: PsiElementPattern.Capture<LeafPsiElement> =
    psiElement(LeafPsiElement::class.java)
        .withSuperParent(5, psiElement(KtAnnotationEntry::class.java)
            .withChild(psiElement(KtConstructorCalleeExpression::class.java)
                .withText(VALUE_ANNOTATION_NAME)))

/**
 * 判断是否有文档型注释
 */
fun PsiElement.hasDocComment(): Boolean {
  return this.firstChild is KDoc || this.firstChild is PsiDocComment || this.firstChild is JSDocComment
}
