/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao

import org.junit.Test

class CodeRearrangeTest {

  val c = ""
  val a = c
  val b = a

  @Test
  fun test() {
    val c = ""
    val a = c
    val b = a
    val dependencyMap = hashMapOf<String, Set<String>>()
    dependencyMap["a"] = setOf("c")
    dependencyMap["b"] = setOf("a", "c")
    dependencyMap["c"] = setOf()
    val list = listOf("c", "a", "b").toSortedSet(Comparator { a, b ->
      println("$a/$b")
      if (dependencyMap[a]!!.contains(b)) {
        1
      } else if (dependencyMap[b]!!.contains(a)) {
        -1
      } else {
        a.compareTo(b)
      }
    })
    println(list)
  }

}
