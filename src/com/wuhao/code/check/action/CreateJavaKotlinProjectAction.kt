package com.wuhao.code.check.action

import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * 根据模板创建java&kotlin项目
 *
 * @author 吴昊
 * @since 1.3.6
 */
class CreateJavaKotlinProjectAction : CreateProjectAction() {

  override fun getTemplateUrl(): String {
    return pluginSettings.javaKotlinTemplateUrl
  }

  override fun onCreated(event: AnActionEvent, prepareCreateInfo: PrepareInfo) {
  }

}

