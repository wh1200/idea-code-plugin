/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.wuhao.code.check.constants.InspectionNames.CODE_FORMAT
import com.wuhao.code.check.constants.registerWarning
import com.wuhao.code.check.inspection.visitor.CodeFormatVisitor
import com.wuhao.code.check.inspection.visitor.CommonCodeFormatVisitor

/**
 * @author 吴昊
 * @since 1.0
 */
class CodeFormatInspection : BaseInspection(CODE_FORMAT) {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return CodeFormatVisitor(CommonCodeFormatVisitor(holder))
  }

}

