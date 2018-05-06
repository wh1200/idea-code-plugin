/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.ui

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.wuhao.code.check.DEFAULT_VUE_TEMPLATE_URL
import com.wuhao.code.check.ui.PluginSettings.Companion.CONFIG_NAME
import org.jdom.Element


/**
 * 配置读取类
 * @author 吴昊
 * @since 1.0
 */
@State(name = CONFIG_NAME,
    storages = [(Storage(file = "\$APP_CONFIG$/aegis.xml"))])
class PluginSettings : PersistentStateComponent<Element> {

  var gitPrivateToken: String = ""
  var vueTemplateUrl: String = ""
    get() {
      return if (field.isEmpty()) DEFAULT_VUE_TEMPLATE_URL else field
    }
  var reactTemplateUrl: String = ""
  var javaKotlinTemplateUrl: String = ""

  override fun getState(): Element? {
    val element = Element(CONFIG_NAME)
    element.setAttribute(GIT_PRIVATE_TOKEN, gitPrivateToken)
    element.setAttribute(VUE_TEMPLATE_URL, vueTemplateUrl)
    element.setAttribute(REACT_TEMPLATE_URL, reactTemplateUrl)
    element.setAttribute(JAVA_KOTLIN_TEMPLATE_URL, javaKotlinTemplateUrl)
    return element
  }

  override fun loadState(state: Element) {
    if (state.getAttributeValue(GIT_PRIVATE_TOKEN) != null) {
      this.gitPrivateToken = state.getAttributeValue(GIT_PRIVATE_TOKEN)
    }
    if (state.getAttributeValue(VUE_TEMPLATE_URL) != null) {
      this.vueTemplateUrl = state.getAttributeValue(VUE_TEMPLATE_URL)
    }
    if (state.getAttributeValue(REACT_TEMPLATE_URL) != null) {
      this.reactTemplateUrl = state.getAttributeValue(REACT_TEMPLATE_URL)
    }
    if (state.getAttributeValue(JAVA_KOTLIN_TEMPLATE_URL) != null) {
      this.javaKotlinTemplateUrl = state.getAttributeValue(JAVA_KOTLIN_TEMPLATE_URL)
    }
  }


  companion object {
    const val CONFIG_NAME = "AegisSettings"
    const val GIT_PRIVATE_TOKEN = "GitPrivateToken"
    const val VUE_TEMPLATE_URL = "VueTemplateUrl"
    const val REACT_TEMPLATE_URL = "ReactTemplateUrl"
    const val JAVA_KOTLIN_TEMPLATE_URL = "JavaKotlinTemplateUrl"
    val instance: PluginSettings
      get() = ServiceManager.getService(PluginSettings::class.java) as PluginSettings
  }

}
