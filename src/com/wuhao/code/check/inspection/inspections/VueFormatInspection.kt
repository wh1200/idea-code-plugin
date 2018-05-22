/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.wuhao.code.check.constants.InspectionNames.VUE_FORMAT
import com.wuhao.code.check.inspection.visitor.CodeFormatVisitor
import com.wuhao.code.check.inspection.visitor.VueCodeFormatVisitor

/**
 * @author 吴昊
 * @since 1.0
 */
class VueFormatInspection : BaseInspection(VUE_FORMAT) {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return CodeFormatVisitor(VueCodeFormatVisitor(holder))
  }

}

