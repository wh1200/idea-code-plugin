/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.less

import java.util.*

/**
 *
 * @author 吴昊
 * @since 1.3.1
 */
class LessArrangementParseInfo {

  val entries: List<LessElementArrangementEntry>
    get() = myEntries
  private val myEntries = ArrayList<LessElementArrangementEntry>()

  fun addEntry(entry: LessElementArrangementEntry) {
    myEntries.add(entry)
  }

}

