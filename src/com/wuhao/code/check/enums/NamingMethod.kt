package com.wuhao.code.check.enums

/**
 * 命名方式
 * Created by 吴昊 on 2018/5/17.
 *
 * @author 吴昊
 * @since 1.3.6
 */
enum class NamingMethod(val zhName: String, private val regex: Regex) {

  /**
   * 驼峰命名法，第一个单词首字母小写，其余首字母大写，例如 myName
   */
  Camel("驼峰", "[a-z0-9]+([A-Z]{1,}[a-z0-9]{0,}){0,}".toRegex()),
  /**
   * 常量命名法，单词全部大写，由下划线（_）连接，例如 MY_NAME
   */
  Constant("静态常量", "[A-Z0-9]+(_[A-Z0-9]+){0,}".toRegex()),
  /**
   * 中划线命名，单词全部小写，由中划线（-）连接，例如 my-name
   */
  Dash("中划线", "[a-z0-9]+(-[a-z0-9]+){0,}".toRegex()),
  /**
   * 帕斯卡命名法，所有单词首字母大写,例如 MyName
   */
  Pascal("帕斯卡", "[A-Z][a-z0-9]+([A-Z]{1,}[a-z0-9]{0,}){0,}".toRegex()),
  /**
   * 下划线命名，单词全部小写，由下划线（_）连接，例如 my_name
   */
  Underline("下划线", "[a-z0-9]+(_[a-z0-9]+){0,}".toRegex());

  fun test(string: String): Boolean {
    return this.regex.matches(string)
  }

}

