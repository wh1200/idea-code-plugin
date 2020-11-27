package com.wuhao.code.check.inspection.fix.java;

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.psi.DataSourceManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiComment
import com.intellij.psi.impl.PsiElementFactoryImpl
import com.wuhao.code.check.insertBefore
import fastjdbc.FastJdbc
import fastjdbc.NoPoolDataSource
import fastjdbc.SimpleFastJdbc
import fastjdbc.handler.RowHandler
import java.sql.ResultSet
import java.util.regex.Pattern

val store = hashMapOf<String, List<String>>()

class SyncDatabaseComment : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.psiElement as PsiClass
    val tableAnnotation = el.getAnnotation("javax.persistence.Table")
    if (tableAnnotation != null) {
      val tableName = tableAnnotation.findAttributeValue("name")!!.text.drop(1).dropLast(1)
      var url: String? = null
      var username: String? = null
      var password: String? = null
      var schema: String? = null
      if (store[project.name] != null) {
        url = store[project.name]!![0]
        username = store[project.name]!![1]
        password = store[project.name]!![2]
        schema = store[project.name]!![3]
      }
      ApplicationManager.getApplication().invokeLater {
        var prepared = false
        if (url.isNullOrBlank() || username.isNullOrBlank() || password.isNullOrBlank() || schema.isNullOrBlank()) {
          val dataSourceManager = DataSourceManager
              .getManagers(project)
          if (dataSourceManager.size > 0 && dataSourceManager[0].dataSources.size > 0) {
            val dataSources = dataSourceManager.get(0).dataSources
            val index = Messages.showChooseDialog(
                "选择数据源", "提示", dataSources.map { it.name }.toTypedArray(),
                null, null
            )
            password = Messages.showPasswordDialog("输入数据库密码", "提示")
            if (password != null) {
              val dataSource: LocalDataSource = dataSources[index] as LocalDataSource
              username = dataSource.username
              url = dataSource.url!!
              val driver = dataSource.driverClass
              Class.forName(driver)
              if (url!!.indexOf("?") < 0) {
                url += "?serverTimezone=UTC"
              } else if (url!!.indexOf("serverTimezone") < 0) {
                url += "&serverTimezone=UTC"
              }
              schema = resolveSchemaFromUrl(url!!)
              if (schema == null) {
                schema = Messages.showInputDialog("输入数据库名称", "提示", null)
              }
            }
            prepared = true
          } else {
            Messages.showErrorDialog("缺少数据源配置", "警告")
          }
        } else {
          prepared = true
        }
        if (prepared) {
          when {
            password.isNullOrBlank() -> {
              Messages.showErrorDialog("请输入密码", "错误")
            }
            schema.isNullOrBlank()   -> {
              Messages.showErrorDialog("请输入Schema", "错误")
            }
            else                     -> {
              val fastJdbc: FastJdbc = SimpleFastJdbc(NoPoolDataSource(url, username, password))
              val columns = findTableSchemas(fastJdbc, schema, tableName)
              if (columns!!.isNotEmpty()) {
                store[project.name] = listOf(url!!, username!!, password!!, schema!!)
                columns.forEach {
                  println(it?.columnName + "/" + it?.columnComment)
                }
                WriteCommandAction.runWriteCommandAction(project) {
                  el.fields.forEach { field ->
                    val col = columns.find {
                      field.name.toLowerCase() == it!!.columnName!!.toLowerCase()
                          || field.name.toLowerCase() == it.columnName!!.replace("_", "")
                    }
                    if (col?.columnComment != null) {
                      val comment = PsiElementFactoryImpl(project)
                          .createCommentFromText("/** " + col.columnComment!! + " */", field)
                      if (field.children[0] !is PsiComment) {
                        comment.insertBefore(field.children[0])
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

    }
  }

  override fun getFamilyName(): String {
    return "同步数据库注释"
  }

  private fun resolveSchemaFromUrl(url: String): String? {
    val mt = Pattern.compile("jdbc:.*://.*/(.*)\\?.*")
        .matcher(url)
    if (mt.find()) {
      return mt.group(1)
    }

    return null
  }

}

class TableRowHandler : RowHandler<ColumnSchema> {

  override fun handle(row: ResultSet, rowNum: Int): ColumnSchema {
    val schema = ColumnSchema()
    schema.columnName = row.getString("COLUMN_NAME")
    schema.columnComment = row.getString("COLUMN_COMMENT")
    return schema
  }

}

fun findTableSchemas(fastJdbc: FastJdbc, database: String?, table: String?): List<ColumnSchema?>? {
  val sql =
      "select column_name, column_comment from information_schema.COLUMNS where table_schema = '${database}' and " +
          "table_name = '${table}'"
  return fastJdbc.find(sql, TableRowHandler())
}

