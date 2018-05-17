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

  val file = """#if (${'$'}{PACKAGE_NAME} && ${'$'}{PACKAGE_NAME} != "")package ${'$'}{PACKAGE_NAME}

#end
#parse("File Header.java")
$DEFAULT_BLOCK_COMMENT""".trimIndent()
  val ktClass = file + "\n" + """class ${'$'}{NAME} {
    }""".trimIndent()

  val ktObject = file + "\n" + """
    object ${'$'}{NAME} {
    }""".trimIndent()
  val enum: String = file + "\n" + """
    enum class ${'$'}{NAME} {
    }""".trimIndent()
  val inter: String = file + "\n" + """
    interface ${'$'}{NAME} {
    }""".trimIndent()

}

