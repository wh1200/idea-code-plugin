/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao

import com.wuhao.code.check.http.HttpRequest
import org.junit.Test
//import kotlin.test.assertFalse
//import kotlin.test.assertTrue

/**
 * Created by 吴昊 on 18-4-25.
 */
class Test5 {

  @Test
  fun regexTest() {
    val constReg = "^[A-Z]+(_[A-Z]+){0,}$".toRegex()
    val camelReg = "^[a-z]+([A-Z0-9]{1,}[a-z0-9]{0,}){0,}$".toRegex()
    val dashReg = "[a-z0-9]+(-[a-z0-9]+){0,}".toRegex()
    val reg = camelReg
//    assertFalse("MY".matches(reg))
//    assertFalse("MY_NAME".matches(reg))
//    assertTrue("my1".matches(reg))
//    assertFalse("my1-bike".matches(reg))
//    assertFalse("my_name".matches(reg))
//    assertTrue("myName12Dad".matches(reg))
//    assertFalse("MyName".matches(reg))
//    assertTrue("userDAO".matches(reg))
//    assertFalse("My".matches(reg))
//    assertFalse("My_".matches(reg))
//    assertFalse("MY_".matches(reg))
//    assertFalse("MY_FAMILY_NAME".matches(reg))
  }

  @Test
  fun test() {
    println(getWordCount("myName"))
    println(getWordCount("MyName"))
    println(getWordCount("My Name"))
    println(getWordCount("My New Name"))
    println(getWordCount("MyNewName"))
    println(getWordCount("MyNewNAME"))
    println(getWordCount("test-myname"))
    println(getWordCount("test-my_name"))
  }

  @Test
  fun test2() {
    var a = 10
    do {
      println(a)
      a--
    } while (a > 1)
  }

  @Test
  fun testReplace() {
    var name = "/a/b/c/D.class"
    name = name.substring(1).replace("/", ".")
    println(name)
  }

  private fun getWordCount(name: String): List<String> {
    return if (name.contains("-") || name.contains("_")) {
      name.split("-|_".toRegex()).map {
        if (it.length == 1) {
          it.toUpperCase()
        } else {
          it.substring(0, 1).toUpperCase() + it.substring(1).toLowerCase()
        }
      }
    } else if (name.isBlank()) {
      listOf()
    } else {
      val result = ArrayList<String>()
      var wordCount = 0
      var upperCaseContinue = false
      var tmpWord = ""
      name.forEachIndexed { index, char ->
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
      if (tmpWord.length > 0 && !result.contains(tmpWord)) {
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

  @Test
  fun sendMsg() {
    val res = HttpRequest.newPost("https://os.aegis-info.com/api/idea/callback")
        .execute()
    println(res.response)
  }

}

