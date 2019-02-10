package com.wuhao.code.check.action

import com.intellij.lang.javascript.buildTools.npm.rc.NpmCommand
import com.intellij.lang.javascript.buildTools.npm.rc.NpmConfigurationType
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfiguration
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 *
 * @author 吴昊
 * @since 1.4.6
 */
class CreateRunConfiguration : AnAction() {

  override fun actionPerformed(event: AnActionEvent) {
    val project = event.project!!
    val configurationFactory = NpmConfigurationType.getInstance()
    val config = configurationFactory.createConfiguration("install",
        configurationFactory.createTemplateConfiguration(project).apply {
          (this as NpmRunConfiguration).runSettings = NpmRunSettings.builder()
              .setCommand(NpmCommand.INSTALL)
              .setPackageJsonPath(project.basePath + "/package.json")
              .build()
        }
    )
//    ProgramRunnerUtil.executeConfiguration(
//        RunnerAndConfigurationSettings()
//    )
  }

}
