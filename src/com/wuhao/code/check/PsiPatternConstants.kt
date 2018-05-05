/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiJavaToken
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.java.PsiAnnotationImpl
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtConstructorCalleeExpression

private const val VALUE_ANNOTATION_NAME = "Value"
/**
 * 匹配Java文件中的@Value注解中的字符串元素
 */
val JAVA_VALUE_ANNOTATION_PATTERN: PsiElementPattern.Capture<PsiJavaToken> =
    PlatformPatterns.psiElement(PsiJavaToken::class.java)
    .withSuperParent(4, PlatformPatterns.psiElement(PsiAnnotationImpl::class.java)
        .withChild(PlatformPatterns.psiElement(PsiJavaCodeReferenceElement::class.java)
            .withText(VALUE_ANNOTATION_NAME)))

val KOTLIN_VALUE_ANNOTATION_PATTERN: PsiElementPattern.Capture<LeafPsiElement> =
    PlatformPatterns.psiElement(LeafPsiElement::class.java)
        .withSuperParent(5, PlatformPatterns.psiElement(KtAnnotationEntry::class.java)
            .withChild(PlatformPatterns.psiElement(KtConstructorCalleeExpression::class.java)
                .withText(VALUE_ANNOTATION_NAME)))
