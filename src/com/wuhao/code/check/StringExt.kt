/**
 * String扩展类
 * Created by 吴昊 on 2018/5/17.
 *
 * @author 吴昊
 * @since
 */
package com.wuhao.code.check

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
 * 如果当前字符串以指定的字符串结束，将将结尾的指定字符串移除
 * @param ends 指定要移除的字符串
 * @return
 */
fun String.removeEnds(ends: String): String {
  return if (this.endsWith(ends)) {
    this.dropLast(ends.length)
  } else {
    this
  }
}


/**
 * 如果当前字符串不是以指定字符串结束，则在其后面添加指定字符串
 * @param ends 指定的字符串
 * @return
 */
fun String.appendIfNotEndsWith(ends: String): String {
  return if (this.endsWith(ends)) {
    this
  } else {
    this + ends
  }
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

