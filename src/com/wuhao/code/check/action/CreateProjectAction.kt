package com.wuhao.code.check.action

import com.intellij.ide.impl.ProjectUtil
import com.intellij.lang.javascript.buildTools.npm.rc.NpmCommand
import com.intellij.lang.javascript.buildTools.npm.rc.NpmConfigurationType
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfiguration
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import com.wuhao.code.check.http.HttpRequest
import com.wuhao.code.check.ui.PluginSettings
import org.apache.http.HttpStatus
import org.jetbrains.kotlin.idea.run.addBuildTask
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URL
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

  protected var pluginSettings = PluginSettings.INSTANCE

  override fun actionPerformed(e: AnActionEvent) {
    val templateUrl = getTemplateUrl()
    if (templateUrl.isBlank()) {
      Messages.showErrorDialog("项目模板地址为空", "错误")
    } else if (!templateUrl.startsWith("http")) {
      Messages.showErrorDialog("项目模板地址不正确，地址应当以http(s)开头", "错误")
    } else {
      val prepareCreateInfo = prepareCreate(e, templateUrl)
      if (prepareCreateInfo != null) {
        this.onCreated(e, prepareCreateInfo)
        openProject(prepareCreateInfo.projectRoot)
      }
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
        return !File("${File(e.project!!.basePath).parentFile.absolutePath}/$input").exists()
      }

    })
  }

  private fun openProject(projectRoot: File) {
    val project = ProjectUtil.openOrImport(projectRoot.absolutePath, null, true)!!
    val configurationFactory = NpmConfigurationType.getInstance()
    val config = configurationFactory.createConfiguration("install",
        configurationFactory.createTemplateConfiguration(project).apply {
          (this as NpmRunConfiguration).runSettings = NpmRunSettings.builder()
              .setCommand(NpmCommand.INSTALL)
              .setPackageJsonPath(projectRoot.path + "/package.json")
              .build()
        }
    )
//    RunManagerEx.getInstanceEx(project)
//        .addConfiguration(
//            RunnerAndConfigurationSettingsImpl()
//        )
    config.checkConfiguration()
    config.addBuildTask()
    config.configurationEditor.snapshot
  }

  private fun prepareCreate(e: AnActionEvent, templateUrl: String): PrepareInfo? {
    val newProjectName = getNewProjectName(e)
    if (newProjectName != null && newProjectName.isNotBlank()) {
      val newProjectRoot = File("${File(e.project!!.basePath).parentFile.absolutePath}/$newProjectName")
      if (newProjectRoot.exists()) {
        Messages.showErrorDialog("${newProjectRoot.absolutePath}已存在", "错误")
      } else {
        val requestUrl = transferUrl(templateUrl)
        val httpResult = HttpRequest.newGet(requestUrl).execute()
        //http://git2.aegis-info.com/template/JavaKotlinTemplate/-/archive/master/JavaKotlinTemplate-master.zip
        //http://git2.aegis-info.com/template/aegis-vue-template.git
        //http://git2.aegis-info.com/template/aegis-vue-template/-/archive/master/aegis-vue-template-master.zip
        //http://git2.aegis-info.com/template/aegis-vue-template/-/archive/master/aegis-vue-template-master.zip
        when {
          httpResult.status == HttpStatus.SC_NOT_FOUND -> Messages.showErrorDialog("模板项目不存在", "新建项目出错")
          httpResult.bytes == null                     -> Messages.showErrorDialog(httpResult.response
              ?: httpResult.exception?.message ?: "未知错误", "下载模板出错")
          else                                         -> try {
            unzip(httpResult.bytes!!, newProjectRoot)
            return PrepareInfo(newProjectName, newProjectRoot)
          } catch (e: Exception) {
            e.printStackTrace()
            Messages.showErrorDialog(e.message, "错误")
          }
        }
      }
    }
    return null
  }

  /**
   * 如果原url为.git结尾的url，则将其转换成zip包下载的url
   * @param templateUrl
   */
  private fun transferUrl(templateUrl: String): String {
    val url = URL(templateUrl)
    if (url.path.endsWith(".zip")) {
      return "${url.protocol}://${url.host}${url.path}?ref=master"
    } else if (url.path.endsWith(".git")) {
      val projectName = url.path.substring(url.path.lastIndexOf("/") + 1)
          .replace(".git", "")
      return "${url.protocol}://${url.host}${url.path.replace(".git", "/-/archive/master/${projectName}-master.zip")}"
    } else {
      return templateUrl
    }
  }

  @Throws(Exception::class)
  private fun unzip(bytes: ByteArray, unzipFileDir: File) {
    try {
      if (!unzipFileDir.exists() || !unzipFileDir.isDirectory) {
        unzipFileDir.mkdirs()
      }
      val zip = ZipInputStream(ByteArrayInputStream(bytes))
      var entry = zip.nextEntry
      while (entry != null) {
        val entryFilePath = unzipFileDir.absolutePath + File.separator + if (!entry.name.contains("/")) {
          entry.name
        } else {
          entry.name.split("/").drop(n = 1).joinToString(File.separator)
        }
        val entryFile = File(entryFilePath)
        if (entry.isDirectory) {
          entryFile.mkdirs()
        } else {
          entryFile.writeBytes(zip.readBytes())
        }
        entry = zip.nextEntry
      }
    } catch (e: Exception) {
      throw  e
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
