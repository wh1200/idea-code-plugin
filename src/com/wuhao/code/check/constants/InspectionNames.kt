package com.wuhao.code.check.constants


/**
 * inspection的名称常量对象
 *
 * @author 吴昊
 * @since
 */
object InspectionNames {

  val CODE_FORMAT = NameData("擎盾代码格式检查", "aegis.code.check.validation")
  val JAVASCRIPT_FORMAT = NameData("Javascript代码格式检查", "aegis.code.check.validation.javascript.format")
  val TYPESCRIPT_FORMAT = NameData("Typescript代码格式检查", "aegis.code.check.validation.typescript.format")
  val VUE_FORMAT = NameData("Vue代码格式检查", "aegis.code.check.validation.vue.format")

  fun values() = listOf(
      CODE_FORMAT, JAVASCRIPT_FORMAT, TYPESCRIPT_FORMAT, VUE_FORMAT
  )

  /**
   * inspection的命名数据
   * @author 吴昊
   * @since
   */
  data class NameData(val displayName: String,
                      val shortName: String)

}

