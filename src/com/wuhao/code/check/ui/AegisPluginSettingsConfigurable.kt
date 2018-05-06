/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.ui

import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent

/**
 * Created by 吴昊 on 18-4-25.
 * @author 吴昊
 * @since 1.0
 */
class AegisPluginSettingsConfigurable : SearchableConfigurable {


  private var aegisPluginSettings: AegisPluginSettings? = null
  private var pluginSettings = PluginSettings.instance

  override fun isModified(): Boolean {
    return this.pluginSettings.gitPrivateToken != aegisPluginSettings?.gitPrivateTokenInput?.text
        || this.pluginSettings.vueTemplateUrl != aegisPluginSettings?.gitPrivateTokenInput?.text
        || this.pluginSettings.reactTemplateUrl != aegisPluginSettings?.reactTemplateUrlInput?.text
        || this.pluginSettings.javaKotlinTemplateUrl != aegisPluginSettings?.javaKotlinTemplateUrlInput?.text
  }

  override fun getId(): String {
    return "Aegis Plugin Configuration"
  }

  override fun getDisplayName(): String {
    return "擎盾开发插件"
  }

  override fun apply() {
    aegisPluginSettings?.let {
      pluginSettings.javaKotlinTemplateUrl = it.javaKotlinTemplateUrlInput.text
      pluginSettings.reactTemplateUrl = it.reactTemplateUrlInput.text
      pluginSettings.gitPrivateToken = it.gitPrivateTokenInput.text
      pluginSettings.vueTemplateUrl = it.vueTemplateUrlInput.text
    }
  }

  override fun createComponent(): JComponent? {
    if (aegisPluginSettings == null) {
      aegisPluginSettings = AegisPluginSettings()
    }
    aegisPluginSettings?.let {
      it.gitPrivateTokenInput.text = pluginSettings.gitPrivateToken
      it.vueTemplateUrlInput.text = pluginSettings.vueTemplateUrl
      it.reactTemplateUrlInput.text = pluginSettings.reactTemplateUrl
      it.javaKotlinTemplateUrlInput.text = pluginSettings.javaKotlinTemplateUrl
    }
    return aegisPluginSettings?.mainPanel
  }
}
