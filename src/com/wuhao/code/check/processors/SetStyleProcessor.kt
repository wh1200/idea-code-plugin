/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.processors

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor

/**
 * 格式化代码前强制设置选项
 *
 * @author 吴昊
 * @since 1.2.6
 */
class SetStyleProcessor : PreFormatProcessor {

  override fun process(element: ASTNode, range: TextRange): TextRange {

//    val project = element.psi.project
//    val settings = CodeStyle.getSettings(project)
//    val commonSettings = settings.getCommonSettings(JavaLanguage.INSTANCE)
//    commonSettings.setArrangementSettings(createSettings())
//    commonSettings.FORCE_REARRANGE_MODE = CommonCodeStyleSettings.REARRANGE_ALWAYS
    return range
  }
}
