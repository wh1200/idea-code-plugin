/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix.kotlin

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project

/**
 * kt文件中多余的分号修复
 * @author 吴昊
 * @since 1.2.4
 */
class KotlinCommaFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    descriptor.psiElement.delete()
  }

  override fun getFamilyName(): String {
    return "删除分号"
  }

}

