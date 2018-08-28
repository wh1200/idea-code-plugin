/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

/**
 * Created by 吴昊 on 18-4-26.
 */
open class CodeFormatVisitor(val visitor: BaseCodeFormatVisitor) : PsiElementVisitor() {

  private var holder: ProblemsHolder? = null

  constructor(visitor: BaseCodeFormatVisitor, holder: ProblemsHolder) : this(visitor) {
    this.holder = holder
  }

  override fun visitElement(element: PsiElement) {
    if (visitor is PsiElementVisitor && visitor.support(element.language)) {
      element.accept(visitor)
    }
  }

}

