package com.wuhao.code.check.ui

import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.model.DasDataSource
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import javax.swing.JComponent
import javax.swing.JRadioButton

class AutoGeneratorSettingsDialog(
    val project: Project?,
    val tableName: String,
    val databases: List<DasDataSource>
) : DialogWrapper(project, true) {

  private var databaseSettings: AegisPluginSettings
  private var radioButtons: ArrayList<JRadioButton>? = null

  init {
    this.databaseSettings = AegisPluginSettings()
  }

  override fun createCenterPanel(): JComponent? {
    return this.databaseSettings.mainPanel
  }

  override fun doOKAction() {
    val button = this.radioButtons!!.find {
      it.isSelected
    }
    val dataSource: LocalDataSource? = this.databases.find {
      it.name == button?.name
    } as LocalDataSource?
    if (dataSource == null) {
      Messages.showErrorDialog("没有选择数据源", "错误")
    }
  }

}
