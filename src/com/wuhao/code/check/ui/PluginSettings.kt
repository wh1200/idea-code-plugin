/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.ui

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.wuhao.code.check.constants.DEFAULT_JAVA_KOTLIN_TEMPLATE_URL
import com.wuhao.code.check.constants.DEFAULT_VUE_TEMPLATE_URL
import com.wuhao.code.check.ui.PluginSettings.Companion.CONFIG_NAME
import org.jdom.Element
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField


/**
 * 配置读取类
 * @author 吴昊
 * @since 1.0
 */
@State(name = CONFIG_NAME,
    storages = [(Storage(file = "\$APP_CONFIG$/aegis.xml"))])
class PluginSettings : PersistentStateComponent<Element> {

  var email: String = ""
  var tapdAccount: String = ""
  var tapdToken: String = ""
  var gitPrivateToken: String = ""
  var javaKotlinTemplateUrl: String = ""
    get() {
      return if (field.isEmpty()) {
        DEFAULT_JAVA_KOTLIN_TEMPLATE_URL
      } else {
        field
      }
    }
  var reactTemplateUrl: String = ""
  var user: String = ""
  var vueTemplateUrl: String = ""
    get() {
      return field.ifEmpty {
        DEFAULT_VUE_TEMPLATE_URL
      }
    }

  companion object {
    const val CONFIG_NAME = "AegisSettings"
    val INSTANCE: PluginSettings
      get() = ServiceManager.getService(PluginSettings::class.java) as PluginSettings
    val NULLABLE_INSTANCE: PluginSettings?
      get() = ServiceManager.getService(PluginSettings::class.java)
  }

  override fun getState(): Element {
    val element = Element(CONFIG_NAME)
    PluginSettings::class.memberProperties.forEach { property ->
      element.setAttribute(property.name, property.get(this) as String)
    }
    return element
  }

  override fun loadState(state: Element) {
    PluginSettings::class.memberProperties.forEach { property ->
      val value = state.getAttributeValue(property.name)
      if (value != null) {
        property.javaField?.set(this, value)
      }
    }
  }

}

