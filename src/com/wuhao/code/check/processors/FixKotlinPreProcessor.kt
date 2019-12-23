/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.processors

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.*
import com.wuhao.code.check.style.arrangement.kotlin.KotlinRecursiveVisitor
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KtTokens.ANDAND
import org.jetbrains.kotlin.lexer.KtTokens.OROR
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * kotlin格式化前预处理
 * @author 吴昊
 * @since 1.3.3
 */
class FixKotlinPreProcessor : PreFormatProcessor {

  override fun process(astNode: ASTNode, textRange: TextRange): TextRange {
    if (isIdea && astNode.psi.language is KotlinLanguage) {
      val factory = astNode.psi.ktPsiFactory
      astNode.psi.accept(KotlinPreFixVisitor(factory))
    }
    return textRange
  }

}

/**
 * kotlin代码修正递归访问器
 * @author 吴昊
 * @since 1.3.3
 */
class KotlinPreFixVisitor(val factory: KtPsiFactory) : KotlinRecursiveVisitor() {

  override fun visitElement(element: PsiElement) {
    if (element is LeafPsiElement && element.elementType in listOf(ANDAND, OROR)) {
      val spaceBefore = element.parent.prevSibling
      val spaceAfter = element.parent.nextSibling
      if (spaceBefore is PsiWhiteSpace && spaceBefore.getLineCount() == 0
          && spaceAfter is PsiWhiteSpace && spaceAfter.getLineCount() == 1) {
        spaceBefore.replace(element.createNewLine())
        spaceAfter.replace(element.createWhiteSpace())
      }
    }
    super.visitElement(element)
  }

}

