package com.wuhao.code.check.action

import com.intellij.database.model.DasDataSource
import com.intellij.database.psi.DataSourceManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.JavaProjectRootsUtil
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.impl.PsiElementFactoryImpl
import com.wuhao.code.check.cachedPosterity
import com.wuhao.code.check.insertBefore
import com.wuhao.code.check.inspection.fix.java.findTableSchemas
import com.wuhao.code.check.inspection.fix.java.store
import com.wuhao.code.check.inspection.visitor.JavaCommentVisitor.Companion.ENTITY_CLASS
import com.wuhao.code.check.toPsiFile
import fastjdbc.FastJdbc
import fastjdbc.NoPoolDataSource
import fastjdbc.SimpleFastJdbc

fun getDataSources(project: Project): List<out DasDataSource> {
  val dataSourceManager = DataSourceManager
      .getManagers(project)
  if (dataSourceManager.size > 0 && dataSourceManager[0].dataSources.size > 0) {
    return dataSourceManager.get(0).dataSources
  }
  return listOf()
}

/**
 * 根据模板创建java&kotlin项目
 *
 * @author 吴昊
 * @since 1.3.6
 */
class SyncDatabaseCommentAction : AnAction() {

  override fun actionPerformed(event: AnActionEvent) {
    val project = event.project!!
    val sourceRoots = JavaProjectRootsUtil.getSuitableDestinationSourceRoots(project)
    val entityClasses = arrayListOf<PsiClass>()
    sourceRoots.forEach { root ->
      if (root is VirtualDirectoryImpl) {
        root.cachedPosterity.forEach { file ->
          val psiFile = file.toPsiFile(project)
          if (psiFile is PsiJavaFile) {
            psiFile.classes.forEach { cls ->
              if (cls.hasAnnotation(ENTITY_CLASS)) {
                entityClasses.add(cls)
              }
            }
          }
        }
      }
    }
    println("共发现持久化类${entityClasses.size}个")
    ApplicationManager.getApplication().invokeLater {
      WriteCommandAction.runWriteCommandAction(project) {
        entityClasses.forEach {
          val tableAnnotation = it.getAnnotation("javax.persistence.Table")
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
            val fastJdbc: FastJdbc = SimpleFastJdbc(NoPoolDataSource(url, username, password))
            val columns = findTableSchemas(fastJdbc, schema, tableName)
            if (columns!!.isNotEmpty()) {
              store[project.name] = listOf(url!!, username!!, password!!, schema!!)
              it.fields.forEach { field ->
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
