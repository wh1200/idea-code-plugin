/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.inspections

import com.intellij.codeInspection.InspectionToolProvider
import com.intellij.codeInspection.LocalInspectionTool
import com.wuhao.code.check.isIdea
import com.wuhao.code.check.vueEnabled

/**
 * @author 吴昊
 * @since 1.0
 */
class CheckProvider : InspectionToolProvider {

  override fun getInspectionClasses(): Array<out Class<out LocalInspectionTool>> {
    val classes = arrayListOf<Class<out LocalInspectionTool>>()
    if (isIdea) {
      classes.add(JavaCommentInspection::class.java)
      classes.add(JavaFormatInspection::class.java)
      classes.add(JavaPropertyClassCreateInspection::class.java)
      classes.add(KotlinCommentInspection::class.java)
      classes.add(KotlinFormatInspection::class.java)
      classes.add(KotlinActionSpecificationInspection::class.java)
      classes.add(CodeFormatInspection::class.java)
      classes.add(MybatisMapperInspection::class.java)
    }
    classes.add(JavaScriptFormatInspection::class.java)
    classes.add(TypeScriptFormatInspection::class.java)
    if (vueEnabled) {
      classes.add(VueFormatInspection::class.java)
    }
    return classes.filter { it.superclass == BaseInspection::class.java }.toTypedArray()
  }

}
