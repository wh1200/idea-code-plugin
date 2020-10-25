package com.wuhao.code.check.action

import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.model.DasDataSource
import com.intellij.database.psi.DataSourceManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.wuhao.code.check.inspection.fix.java.ColumnSchema
import com.wuhao.code.check.inspection.fix.java.TableRowHandler
import fastjdbc.FastJdbc
import fastjdbc.NoPoolDataSource
import fastjdbc.SimpleFastJdbc

/**
 * 根据模板创建java&kotlin项目
 *
 * @author 吴昊
 * @since 1.3.6
 */
class SyncDatabaseCommentAction : AnAction() {

  override fun actionPerformed(event: AnActionEvent) {

  }

  fun getDataSources(project: Project): List<out DasDataSource> {
    val dataSourceManager = DataSourceManager
        .getManagers(project)
    if (dataSourceManager.size > 0 && dataSourceManager[0].dataSources.size > 0) {
      return dataSourceManager.get(0).dataSources
    }
    return listOf()
  }

}
