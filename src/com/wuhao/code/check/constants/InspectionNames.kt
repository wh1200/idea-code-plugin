package com.wuhao.code.check.constants

import com.wuhao.code.check.style.collectFields

/**
 * inspection的名称常量对象
 *
 * @author 吴昊
 * @since
 */
object InspectionNames {

  val CODE_FORMAT = NameData("擎盾代码格式检查", "aegis.code.check.validation")
  val JAVASCRIPT_FORMAT = NameData("Javascript代码格式检查", "aegis.code.check.validation.javascript.format")
  val JAVA_COMMENT = NameData("Java注释检查", "aegis.code.check.validation.java")
  val JAVA_FORMAT = NameData("Java代码格式检查", "aegis.code.check.validation.java.format")
  val KOTLIN_COMMENT = NameData("Kotlin注释检查", "aegis.code.check.validation.kotlin")
  val KOTLIN_FORMAT = NameData("Kotlin代码格式检查", "aegis.code.check.validation.kotlin.format")
  val PROPERTY_CLASS = NameData("属性名称对象", "aegis.code.check.validation.name.object")
  val JAVA_PROPERTY_CLASS = NameData("Java属性名称类", "aegis.code.check.validation.name.java.object")
  val TYPESCRIPT_FORMAT = NameData("Typescript代码格式检查", "aegis.code.check.validation.typescript.format")
  val VUE_FORMAT = NameData("Vue代码格式检查", "aegis.code.check.validation.vue.format")

  fun values() = collectFields<NameData>(InspectionNames::class.java)

  /**
   * inspection的命名数据
   * @author 吴昊
   * @since
   */
  data class NameData(val displayName: String,
                      val shortName: String)

}

