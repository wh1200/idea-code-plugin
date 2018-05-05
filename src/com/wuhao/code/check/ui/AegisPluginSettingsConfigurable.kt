/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.ui

import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent

/**
 * Created by 吴昊 on 18-4-25.
 */
class AegisPluginSettingsConfigurable : SearchableConfigurable {


  private var aegisPluginSettings: AegisPluginSettings? = null
  private var pluginSettings = PluginSettings.instance

  override fun isModified(): Boolean {
    return this.pluginSettings.getGitPrivateToken() != aegisPluginSettings?.gitPrivateTokenInput?.text
        || this.pluginSettings.getVueTemplateUrl() != aegisPluginSettings?.gitPrivateTokenInput?.text
        || this.pluginSettings.getReactTemplateUrl() != aegisPluginSettings?.reactTemplateUrlInput?.text
        || this.pluginSettings.getJavaKotlinTemplateUrl() != aegisPluginSettings?.javaKotlinTemplateUrlInput?.text
        || this.pluginSettings.getJavaTemplateUrl() != aegisPluginSettings?.javaTemplateUrlInput?.text
        || this.pluginSettings.getKotlinTemplateUrl() != aegisPluginSettings?.kotlinTemplateUrlInput?.text
  }

  override fun getId(): String {
    return "Aegis Plugin Configuration"
  }

  override fun getDisplayName(): String {
    return "擎盾开发插件"
  }

  override fun apply() {
    aegisPluginSettings?.let {
      pluginSettings.setJavaKotlinTemplateUrl(it.javaKotlinTemplateUrlInput.text)
      pluginSettings.setKotlinTemplateUrl(it.kotlinTemplateUrlInput.text)
      pluginSettings.setJavaTemplateUrl(it.javaTemplateUrlInput.text)
      pluginSettings.setReactTemplateUrl(it.reactTemplateUrlInput.text)
      pluginSettings.setGitPrivateToken(it.gitPrivateTokenInput.text)
      pluginSettings.setVueTemplateUrl(it.vueTemplateUrlInput.text)
    }
  }

  override fun createComponent(): JComponent? {
    if (aegisPluginSettings == null) {
      aegisPluginSettings = AegisPluginSettings()
    }
    aegisPluginSettings?.let {
      it.gitPrivateTokenInput.text = pluginSettings.getGitPrivateToken()
      it.vueTemplateUrlInput.text = pluginSettings.getVueTemplateUrl()
      it.reactTemplateUrlInput.text = pluginSettings.getReactTemplateUrl()
      it.javaTemplateUrlInput.text = pluginSettings.getJavaTemplateUrl()
      it.kotlinTemplateUrlInput.text = pluginSettings.getKotlinTemplateUrl()
      it.javaKotlinTemplateUrlInput.text = pluginSettings.getJavaKotlinTemplateUrl()
    }
    return this.aegisPluginSettings?.mainPanel
  }


}
