/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement

import java.util.*

/**
 * @author Denis Zhdanov
 * @since 9/26/12 5:47 PM
 */
class KotlinArrangementOverriddenMethodsInfo(val name: String) {

  private val myMethodEntries = ArrayList<KotlinElementArrangementEntry>()

  val methodEntries: List<KotlinElementArrangementEntry>
    get() = myMethodEntries

  fun addMethodEntry(entry: KotlinElementArrangementEntry) {
    myMethodEntries.add(entry)
  }

  override fun toString(): String {
    return "methods from $name"
  }
}
