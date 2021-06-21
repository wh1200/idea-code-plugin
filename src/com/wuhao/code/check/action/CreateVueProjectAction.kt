/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.action

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.intellij.openapi.actionSystem.AnActionEvent
import java.io.File


/**
 * 根据模板创建vue项目
 * Created by 吴昊 on 18-4-24.
 * @author 吴昊
 * @since 1.0
 */
class CreateVueProjectAction : CreateProjectAction() {

  override fun getTemplateUrl(): String {
    return pluginSettings.vueTemplateUrl
  }

  override fun onCreated(event: AnActionEvent, prepareCreateInfo: PrepareInfo) {
    modifyPackageJson(prepareCreateInfo.projectRoot, prepareCreateInfo.projectName)
//    createConfig(prepareCreateInfo.projectRoot, prepareCreateInfo.projectName)
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
    val el = JsonParser.parseString(packageJsonFile.readText())
    el.asJsonObject.addProperty("name", newProjectName)
    val gson = GsonBuilder().setPrettyPrinting().create()
    packageJsonFile.writeText(gson.toJson(el))
  }

}

