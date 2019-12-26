package com.wuhao.code.check.template

/**
 * kotlin文本模板
 * @author 吴昊
 * @since 1.3.6
 */
object JavaTemplates {

  fun getAnnotation(): String {
    return getTemplateContent("annotation")
  }

  fun getClass(): String {
    return getTemplateContent("class")
  }

  fun getController(): String {
    return getTemplateContent("controller")
  }

  fun getEnum(): String {
    return getTemplateContent("enum")
  }

  fun getInterface(): String {
    return getTemplateContent("interface")
  }

  fun getTemplateContent(name: String): String {
    val comment = javaClass.classLoader.getResource("/resources/templates/comment.tmpl")!!.readText()
    val text = javaClass.classLoader.getResource("/resources/templates/${name}.tmpl")!!.readText()
    return text.replace("\${COMMENT}", comment)
  }

  fun getService(): String {
    return getTemplateContent("service")
  }

  fun getEntity(): String {
    return getTemplateContent("entity")
  }

}
