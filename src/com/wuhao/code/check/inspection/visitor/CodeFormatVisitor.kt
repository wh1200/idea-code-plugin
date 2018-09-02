/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.wuhao.code.check.isIdea
import org.jetbrains.kotlin.idea.KotlinLanguage

/**
 * Created by 吴昊 on 18-4-26.
 */
open class CodeFormatVisitor(val visitor: BaseCodeFormatVisitor) : PsiElementVisitor() {

  private var holder: ProblemsHolder? = null
  private val spaceChecker = SpaceChecker()

  constructor(visitor: BaseCodeFormatVisitor, holder: ProblemsHolder) : this(visitor) {
    this.holder = holder
  }

  override fun visitElement(element: PsiElement) {
    if (visitor is PsiElementVisitor && visitor.support(element.language)) {
      if (isIdea) {
        if (element.language is JavaLanguage || element.language is KotlinLanguage) {
          if (holder != null) {
            spaceChecker.checkSpace(element, holder!!)
          }
        }
      }
      element.accept(visitor)
    }
  }

}

