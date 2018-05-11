/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.action

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
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
import com.wuhao.code.check.ui.PluginSettings
import wuhao.tools.http.HttpRequest
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream


/**
 * Created by 吴昊 on 18-4-24.
 */
class CreateVueProjectAction : AnAction() {

  private var pluginSettings = PluginSettings.instance

  override fun actionPerformed(e: AnActionEvent) {
    val newProjectName = Messages.showInputDialog(e.project, "输入项目名称", "输入", null,
        null, object : InputValidator {

      override fun checkInput(input: String?): Boolean {
        return !File("${File(e.project!!.baseDir.path).parentFile.absolutePath}/$input").exists()
      }

      override fun canClose(input: String?): Boolean {
        return true
      }

    })
    if (newProjectName != null) {
      val newProjectRoot = File("${File(e.project!!.baseDir.path).parentFile.absolutePath}/$newProjectName")
      if (newProjectRoot.exists()) {
        Messages.showErrorDialog("${newProjectRoot.absolutePath}已存在", "错误")
      } else {
        val httpResult = HttpRequest.newGet(pluginSettings.vueTemplateUrl)
            .withHeaaer("Private-Token", pluginSettings.gitPrivateToken).execute()
        if (httpResult.bytes == null) {
          Messages.showErrorDialog(httpResult.response, "下载模板出错")
        } else {
          unzip(httpResult.bytes, newProjectRoot)
          modifyPackageJson(newProjectRoot, newProjectName)
          createConfig(newProjectRoot, newProjectName)
          val descriptor = OpenProjectFileChooserDescriptor(false)
          val project = e.getData(CommonDataKeys.PROJECT) as Project
          val vs = LocalFileSystem.getInstance().findFileByIoFile(newProjectRoot)
          FileChooser.chooseFiles(descriptor, project, vs) { var1x ->
            PlatformProjectOpenProcessor.getInstance()
                .doOpenProject(var1x[0] as VirtualFile, project, true)
          }
        }
      }
    }
  }

  private fun createConfig(newProjectRoot: File, newProjectName: String?) {
    val configDir = File(newProjectRoot.absolutePath + File.separator + ".idea")
    if (!configDir.exists()) {
      configDir.mkdir()
    }
    val configFile = File(configDir.absolutePath + File.separator + "$newProjectName.iml")
    if (!configFile.exists()) {
      configFile.writeText("""
<?xml version="1.0" encoding="UTF-8"?>
<module type="WEB_MODULE" version="4">
  <component name="NewModuleRootManager" inherit-compiler-output="true">
    <exclude-output />
    <content url="file://${'$'}MODULE_DIR${'$'}">
      <sourceFolder url="file://${'$'}MODULE_DIR${'$'}/src" isTestSource="false" />
      <sourceFolder url="file://${'$'}MODULE_DIR${'$'}/test" isTestSource="true" />
    </content>
    <orderEntry type="sourceFolder" forTests="false" />
  </component>
</module>
      """.trimIndent())
    }
  }

  private fun modifyPackageJson(newProjectRoot: File, newProjectName: String?) {
    val packageJsonFile = File(newProjectRoot.absolutePath + File.separator + "package.json")
    val el = JsonParser().parse(packageJsonFile.readText())
    el.asJsonObject.addProperty("name", newProjectName)
    val gson = GsonBuilder().setPrettyPrinting().create()
    packageJsonFile.writeText(gson.toJson(el))
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

}

