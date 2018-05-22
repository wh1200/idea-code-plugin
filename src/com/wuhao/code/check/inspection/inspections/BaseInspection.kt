/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.inspections

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.daemon.impl.SeverityRegistrar
import com.intellij.codeInspection.LocalInspectionTool
import com.wuhao.code.check.PluginStart
import com.wuhao.code.check.constants.InspectionNames

/**
 * Created by 吴昊 on 2017/7/19.
 * @author 吴昊
 * @since 1.2
 */
abstract class BaseInspection(private val nameData: InspectionNames.NameData) :
    LocalInspectionTool() {

  override fun getDefaultLevel(): HighlightDisplayLevel {
    val severityRegistrar = SeverityRegistrar.getSeverityRegistrar(null)
    val severity = severityRegistrar.getSeverity(PluginStart.CODE_FORMAT_SEVERITY_NAME)
    return if (severity == null) {
      HighlightDisplayLevel.ERROR
    } else {
      HighlightDisplayLevel(severity)
    }
  }

  override fun getDisplayName(): String {
    return nameData.displayName
  }

  override fun getGroupDisplayName(): String {
    return "擎盾代码检查"
  }

  override fun getShortName(): String {
    return nameData.shortName
  }

  override fun isEnabledByDefault(): Boolean {
    return true
  }

}

