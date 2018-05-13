/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection

import com.intellij.codeInspection.InspectionToolProvider

/**
 * @author 吴昊
 * @since 1.0
 */
class CheckProvider : InspectionToolProvider {

  override fun getInspectionClasses(): Array<Class<*>> {
    return arrayOf(
        CodeFormatInspection::class.java,
        JavaFormatInspection::class.java,
        JavaCommentInspection::class.java,
        KotlinCommentInspection::class.java,
        KotlinFormatInspection::class.java,
        VueFormatInspection::class.java,
        JavaScriptFormatInspection::class.java,
        TypeScriptFormatInspection::class.java,
        PropertyClassCreateInspection::class.java
    )
  }

}

