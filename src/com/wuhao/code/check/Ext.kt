/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check

/**
 * 获取字符串里的单词
 * @param string 待处理的字符串
 * @return 字符串里包含的单词列表
 */
fun getWords(string: String): List<String> {
  return if (string.contains("-") || string.contains("_")) {
    string.split("-|_".toRegex()).map {
      if (it.length == 1) {
        it.toUpperCase()
      } else {
        it.substring(0, 1).toUpperCase() + it.substring(1).toLowerCase()
      }
    }
  } else if (string.isBlank()) {
    listOf()
  } else {
    val result = ArrayList<String>()
    var wordCount = 0
    var upperCaseContinue = false
    var tmpWord = ""
    string.forEachIndexed { index, char ->
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

