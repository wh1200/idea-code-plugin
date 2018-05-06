/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.wuhao.code.check.inspection.visitor.*

/**
 * Created by 吴昊 on 18-4-26.
 */
open class CodeFormatVisitor(holder: ProblemsHolder) : PsiElementVisitor() {

  private val visitors = listOf(
      JavaOrKotlinCodeFormatVisitor(holder),
      JavaCodeFormatVisitor(holder),
      KotlinCodeFormatVisitor(holder),
      VueCodeFormatVisitor(holder),
      TypeScriptCodeFormatVisitor(holder),
      JavaScriptCodeFormatVisitor(holder)
  )

  override fun visitElement(element: PsiElement) {
    super.visitElement(element)
    visitors.forEach { visitor ->
      if (visitor.support(element.language)) {
        visitor.visit(element)
      }
    }
  }
}
