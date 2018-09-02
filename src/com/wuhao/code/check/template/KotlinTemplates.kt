package com.wuhao.code.check.template

/**
 * kotlin文本模板
 * @author 吴昊
 * @since 1.3.6
 */
object KotlinTemplates {

  private const val DEFAULT_BLOCK_COMMENT = """/**
 *
 * Created by ${'$'}{USER} on ${'$'}{DATE}.
 *
 * @author ${'$'}{USER}
 * @since
 */
  """

  val FILE = """#if (${'$'}{PACKAGE_NAME} && ${'$'}{PACKAGE_NAME} != "")package ${'$'}{PACKAGE_NAME}

#end
$DEFAULT_BLOCK_COMMENT""".trimIndent()
  val CLASS = FILE + "\n" + """class ${'$'}{NAME} {
    }""".trimIndent()

  val OBJECT = FILE + "\n" + """
    object ${'$'}{NAME} {
    }""".trimIndent()
  val ENUM: String = FILE + "\n" + """
    enum class ${'$'}{NAME} {
    }""".trimIndent()
  val INTERFACE: String = FILE + "\n" + """
    interface ${'$'}{NAME} {
    }""".trimIndent()

}

