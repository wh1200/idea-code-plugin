package com.wuhao.code.check.action

import com.intellij.ide.actions.OpenProjectFileChooserDescriptor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.PlatformProjectOpenProcessor
import com.wuhao.code.check.http.HttpRequest
import com.wuhao.code.check.http.HttpResult
import com.wuhao.code.check.ui.PluginSettings
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream

/**
 * 根据模板创建项目.
 * 项目模板来自git，从git下载模板项目并根据项目名称修改对应的配置
 * 项目目录创建完成后打开新的项目
 *
 * @author 吴昊
 * @since 1.3.6
 */
abstract class CreateProjectAction : AnAction() {

  protected var pluginSettings = PluginSettings.instance

  override fun actionPerformed(e: AnActionEvent) {
    val prepareCreateInfo = prepareCreate(e, getTemplateUrl())
    if (prepareCreateInfo != null) {
      this.onCreated(e, prepareCreateInfo)
      openProject(e, prepareCreateInfo.projectRoot)
    }
  }

  /**
   * 获取模板项目的git下载地址（zip文件下载地址）
   */
  abstract fun getTemplateUrl(): String

  abstract fun onCreated(event: AnActionEvent, prepareCreateInfo: PrepareInfo)

  private fun getNewProjectName(e: AnActionEvent): String? {
    return Messages.showInputDialog(e.project, "输入项目名称", "输入", null,
        null, object : InputValidator {

      override fun canClose(input: String?): Boolean {
        return true
      }

      override fun checkInput(input: String?): Boolean {
        return !File("${File(e.project!!.baseDir.path).parentFile.absolutePath}/$input").exists()
      }

    })
  }

  private fun openProject(event: AnActionEvent, projectRoot: File) {
    val descriptor = OpenProjectFileChooserDescriptor(false)
    val project = event.getData(CommonDataKeys.PROJECT) as Project
    val vs = LocalFileSystem.getInstance().findFileByIoFile(projectRoot)
    FileChooser.chooseFiles(descriptor, project, vs) { var1x ->
      PlatformProjectOpenProcessor.getInstance()
          .doOpenProject(var1x[0] as VirtualFile, project, true)
    }
  }

  private fun prepareCreate(e: AnActionEvent, templateUrl: String): PrepareInfo? {
    val newProjectName = getNewProjectName(e)
    if (newProjectName != null) {
      val newProjectRoot = File("${File(e.project!!.baseDir.path).parentFile.absolutePath}/$newProjectName")
      if (newProjectRoot.exists()) {
        Messages.showErrorDialog("${newProjectRoot.absolutePath}已存在", "错误")
      } else {
        val httpResult: HttpResult = HttpRequest.newGet(templateUrl)
            .withHeader("Private-Token", pluginSettings.gitPrivateToken).execute()
        if (httpResult.bytes == null) {
          Messages.showErrorDialog(httpResult.response, "下载模板出错")
        } else {
          unzip(httpResult.bytes!!, newProjectRoot)
          return PrepareInfo(newProjectName, newProjectRoot)
        }
      }
    }
    return null
  }

  @Throws(Exception::class)
  private fun unzip(bytes: ByteArray, unzipFileDir: File) {
    if (!unzipFileDir.exists() || !unzipFileDir.isDirectory) {
      unzipFileDir.mkdirs()
    }
    val zip = ZipInputStream(ByteArrayInputStream(bytes))
    var entry = zip.nextEntry
    while (entry != null) {
      val entryFilePath = unzipFileDir.absolutePath + File.separator + entry.name.split(File.separator)
          .drop(n = 1).joinToString(File.separator)
      val entryFile = File(entryFilePath)
      if (entry.isDirectory) {
        entryFile.mkdirs()
      } else {
        entryFile.writeBytes(zip.readBytes())
      }
      entry = zip.nextEntry
    }
  }

  /**
   * 创建信息预备信息
   * @author 吴昊
   * @since 1.3.6
   * @param projectName 项目名称
   * @param projectRoot 项目路径
   */
  data class PrepareInfo(val projectName: String, val projectRoot: File)

}

