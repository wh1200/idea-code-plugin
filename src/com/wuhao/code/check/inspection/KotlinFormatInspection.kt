/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.wuhao.code.check.inspection.visitor.CodeFormatVisitor
import com.wuhao.code.check.inspection.visitor.KotlinCodeFormatVisitor

/**
 * @author 吴昊
 * @since 1.0
 */
class KotlinFormatInspection : BaseInspection(
    "Kotlin代码格式检查",
    "aegis.code.check.validation.kotlin") {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return CodeFormatVisitor(KotlinCodeFormatVisitor(holder), holder)
  }

}

