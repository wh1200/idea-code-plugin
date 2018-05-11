/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.processors

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.wuhao.code.check.style.arrangement.kotlin.KotlinRecursiveVisitor
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtPsiFactory

/**
 * kotlin格式化前预处理
 * @author 吴昊
 * @since 1.3.3
 */
class FixKotlinPreProcessor : PreFormatProcessor {

  override fun process(astNode: ASTNode, textRange: TextRange): TextRange {
    if (astNode.psi.language is KotlinLanguage) {
      val factory = KtPsiFactory(astNode.psi.project)
      astNode.psi.accept(KotlinPreFixVisitor(factory))
    }
    return textRange
  }

  companion object {
    private const val KOTLIN_PRE_FIX_EXECUTED = "KOTLIN_PRE_FIX_EXECUTED"
  }

}

/**
 * kotlin代码修正递归访问器
 * @author 吴昊
 * @since 1.3.3
 */
class KotlinPreFixVisitor(val factory: KtPsiFactory) : KotlinRecursiveVisitor()

