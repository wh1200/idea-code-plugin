package com.wuhao.code.check.template

/**
 * kotlin文本模板
 * @author 吴昊
 * @since 1.3.6
 */
object KotlinTemplates {

  fun getClass(): String = JavaTemplates.getTemplateContent("kotlin_class")
  fun getEnum(): String = JavaTemplates.getTemplateContent("kotlin_enum")
  fun getFile(): String = JavaTemplates.getTemplateContent("kotlin_file")
  fun getInterface(): String = JavaTemplates.getTemplateContent("kotlin_interface")

}
