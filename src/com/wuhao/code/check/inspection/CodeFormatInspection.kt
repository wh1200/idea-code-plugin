/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor

/**
 * @author 吴昊
 * @since 1.0
 */
class CodeFormatInspection : BaseInspection(
  "擎盾代码格式检查",
  "aegis.code.check.validation") {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return CodeFormatVisitor(holder)
  }

  companion object {
    const val MAX_LINES_PER_FILE = 800
    const val MAX_LINES_PER_FUNCTION = 100
  }
}
