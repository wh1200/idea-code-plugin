/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.daemon.GroupNames
import com.intellij.codeInspection.LocalInspectionTool

/**
 * Created by 吴昊 on 2017/7/19.
 * @author 吴昊
 * @since 1.2
 */
abstract class BaseInspection(private val display: String, private val short: String) : LocalInspectionTool() {

  override fun getDefaultLevel(): HighlightDisplayLevel {
    return HighlightDisplayLevel.ERROR
  }

  override fun getDisplayName(): String {
    return display
  }

  override fun getGroupDisplayName(): String {
    return GroupNames.ERROR_HANDLING_GROUP_NAME
  }

  override fun getShortName(): String {
    return short
  }

  override fun isEnabledByDefault(): Boolean {
    return true
  }

}

