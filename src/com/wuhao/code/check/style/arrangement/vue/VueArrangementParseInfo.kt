/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement.vue

import java.util.*

/**
 *
 * @author 吴昊
 * @since 1.3.1
 */
class VueArrangementParseInfo {

  val entries: List<VueElementArrangementEntry>
    get() = myEntries
  private val myEntries = ArrayList<VueElementArrangementEntry>()

  fun addEntry(entry: VueElementArrangementEntry) {
    myEntries.add(entry)
  }
}

