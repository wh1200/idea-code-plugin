/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.fix.vue

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.xml.XmlAttribute
import com.wuhao.code.check.lang.vue.VueDirectives

/**
 * vue指令缩写修复
 * @author 吴昊
 * @since 1.3.2
 */
class VueShortAttrFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val factory = XmlElementFactory.getInstance(project)
    val attr = descriptor.psiElement as XmlAttribute
    if (attr.value != null) {
      val newAttr = factory.createAttribute(VueDirectives.getShortName(attr.name),
          attr.value!!, attr.parent)
      descriptor.psiElement.replace(newAttr)
    }
  }

  override fun getFamilyName(): String {
    return "缩写"
  }

}
