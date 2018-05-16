package com.wuhao.code.check.template

/**
 * kotlin文本模板
 * @author 吴昊
 * @since 1.3.6
 */
object KotlinTemplates {

  private val DEFAULT_BLOCK_COMMENT = """/**
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
  val klass = file + "\n" + """class ${'$'}{NAME} {
    }""".trimIndent()

  val kobject = file + "\n" + """
    object ${'$'}{NAME} {
    }""".trimIndent()
  val enum: String = file + "\n" + """
    enum class ${'$'}{NAME} {
    }""".trimIndent()
  val inter: String = file + "\n" + """
    interface ${'$'}{NAME} {
    }""".trimIndent()

}

