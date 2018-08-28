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
  private var settings = PluginSettings.INSTANCE

  override fun apply() {
    aegisPluginSettings?.let {
      settings.reactTemplateUrl = it.reactTemplateUrlInput.text
      settings.gitPrivateToken = it.gitPrivateTokenInput.text
      settings.vueTemplateUrl = it.vueTemplateUrlInput.text
      settings.user = it.userInput.text
      settings.email = it.emailInput.text
    }
  }

  override fun createComponent(): JComponent? {
    if (aegisPluginSettings == null) {
      aegisPluginSettings = AegisPluginSettings()
    }
    aegisPluginSettings?.let {
      it.gitPrivateTokenInput.text = settings.gitPrivateToken
      it.vueTemplateUrlInput.text = settings.vueTemplateUrl
      it.reactTemplateUrlInput.text = settings.reactTemplateUrl
      it.userInput.text = settings.user
      it.emailInput.text = settings.email
    }
    return aegisPluginSettings?.mainPanel
  }

  override fun getDisplayName(): String {
    return "擎盾开发插件"
  }

  override fun getId(): String {
    return "Aegis Plugin Configuration"
  }

  override fun isModified(): Boolean {
    return settings.gitPrivateToken != aegisPluginSettings?.gitPrivateTokenInput?.text
        || settings.vueTemplateUrl != aegisPluginSettings?.gitPrivateTokenInput?.text
        || settings.reactTemplateUrl != aegisPluginSettings?.reactTemplateUrlInput?.text
        || settings.user == aegisPluginSettings?.userInput?.text
  }

}

