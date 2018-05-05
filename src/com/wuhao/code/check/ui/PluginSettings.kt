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


@State(name = CONFIG_NAME,
    storages = [(Storage(file = "\$APP_CONFIG$/aegis.xml"))])
class PluginSettings : PersistentStateComponent<Element> {

  private var gitPrivateToken: String = ""
  private var vueTemplateUrl: String = ""
  private var reactTemplateUrl: String = ""
  private var javaTemplateUrl: String = ""
  private var kotlinTemplateUrl: String = ""
  private var javaKotlinTemplateUrl: String = ""

  override fun getState(): Element? {
    val element = Element(CONFIG_NAME)
    element.setAttribute(GIT_PRIVATE_TOKEN, gitPrivateToken)
    return element
  }

  override fun loadState(state: Element) {
    if (state.getAttributeValue(GIT_PRIVATE_TOKEN) != null) {
      this.gitPrivateToken = state.getAttributeValue(GIT_PRIVATE_TOKEN)
    }
  }

  fun getGitPrivateToken(): String {
    return gitPrivateToken
  }

  fun getVueTemplateUrl(): String {
    return if(vueTemplateUrl.isEmpty()) DEFAULT_VUE_TEMPLATE_URL else vueTemplateUrl
  }

  fun getReactTemplateUrl(): String {
    return reactTemplateUrl
  }

  fun getJavaTemplateUrl(): String {
    return javaTemplateUrl
  }

  fun getKotlinTemplateUrl(): String {
    return kotlinTemplateUrl
  }

  fun getJavaKotlinTemplateUrl(): String {
    return javaKotlinTemplateUrl
  }

  fun setVueTemplateUrl(vueTemplateUrl: String) {
    this.vueTemplateUrl = vueTemplateUrl
  }

  fun setReactTemplateUrl(reactTemplateUrl: String) {
    this.reactTemplateUrl = reactTemplateUrl
  }

  fun setJavaTemplateUrl(javaTemplateUrl: String) {
    this.javaTemplateUrl = javaTemplateUrl
  }

  fun setKotlinTemplateUrl(kotlinTemplateUrl: String) {
    this.kotlinTemplateUrl = kotlinTemplateUrl
  }

  fun setJavaKotlinTemplateUrl(javaKotlinTemplateUrl: String) {
    this.javaKotlinTemplateUrl = javaKotlinTemplateUrl
  }

  fun setGitPrivateToken(gitPrivateToken: String) {
    this.gitPrivateToken = gitPrivateToken
  }

  companion object {
    const val CONFIG_NAME = "AegisSettings"
    const val GIT_PRIVATE_TOKEN = "GitPrivateToken"
    val instance: PluginSettings
      get() = ServiceManager.getService(PluginSettings::class.java) as PluginSettings
  }

}
