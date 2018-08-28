/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package error

/**
 *
 * @author 吴昊
 * @since
 */
class KtWhiteSpace {

  fun test() {
    val a = 1
    val b = a > 1
    val c = a > 1
    val d = a > 1
    val e = b || c || d
    val list1 = listOf<String>()
    val list2 = listOf<String>()
  }

  companion object {

    const val zh = ""
    const val book = zh
    const val doc = book

  }

}

