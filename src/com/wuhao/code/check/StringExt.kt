/**
 * String扩展类
 * Created by 吴昊 on 2018/5/17.
 *
 * @author 吴昊
 * @since
 */
package com.wuhao.code.check

/**
 * 判断当前字符串是否驼峰命名
 */
val String.isCamelCase: Boolean
  get() {
    return if (this.length == 1 && this.toLowerCase() == this) {
      true
    } else {
      this.matches("^[a-z]+([A-Za-z0-9]+)\$".toRegex())
    }
  }

/**
 * 将字符串转化为驼峰命名
 * @return 转化为驼峰命名的字符串
 */
fun String.toCamelCase(): String {
  return if (!this.isBlank()) {
    val words = getWords().toMutableList()
    words[0] = words[0].toLowerCase()
    words.joinToString("")
  } else {
    ""
  }
}

/**
 * 将字符串转化为帕斯卡命名
 * @return 转化为帕斯卡命名的字符串
 */
fun String.toPascalCase(): String {
  return getWords().joinToString("")
}

/**
 * 将字符串转化为常量命名
 * @return 转化为常量命名的字符串
 */
fun String.toConstantCase(): String {
  return getWords().map { it.toUpperCase() }.joinToString("_")
}

/**
 * 将字符串转化为中划线命名
 * @return 转化为中划线命名的字符串
 */
fun String.toDashCase(): String {
  return getWords().map { it.toLowerCase() }.joinToString("-")
}

/**
 * 将字符串转化为下划线命名
 * @return 转化为下划线命名的字符串
 */
fun String.toUnderlineCase(): String {
  return toConstantCase().toUpperCase()
}

/**
 * 获取当前字符串里的单词，将每个单词的首字母大写
 * @return 字符串里包含的单词列表
 */
fun String.getWords(): List<String> {
  return if (this.contains("-") || this.contains("_")) {
    this.split("-|_".toRegex()).map {
      if (it.length == 1) {
        it.toUpperCase()
      } else {
        it.substring(0, 1).toUpperCase() + it.substring(1).toLowerCase()
      }
    }
  } else if (this.isBlank()) {
    listOf()
  } else {
    val result = ArrayList<String>()
    var wordCount = 0
    var upperCaseContinue = false
    var tmpWord = ""
    this.forEachIndexed { index, char ->
      if (char == ' ') {
        upperCaseContinue = false
        if (tmpWord.isNotEmpty()) {
          result.add(tmpWord)
          tmpWord = ""
        }
      } else if (char in 'a'..'z' || char in '0'..'9') {
        if (index == 0) {
          wordCount++
        }
        tmpWord += char
        upperCaseContinue = false
      } else if (char in 'A'..'Z') {
        if (!upperCaseContinue) {
          wordCount++
          if (tmpWord.isNotEmpty()) {
            result.add(tmpWord)
          }
          tmpWord = "$char"
        } else {
          tmpWord += char
        }
        upperCaseContinue = true
      }
    }
    if (tmpWord.isNotEmpty() && !result.contains(tmpWord)) {
      result.add(tmpWord)
    }
    result.map {
      if (it.length == 1) {
        it.toUpperCase()
      } else {
        it.substring(0, 1).toUpperCase() + it.substring(1).toLowerCase()
      }
    }
  }
}

