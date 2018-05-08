/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement

import java.util.*

/**
 * @author Denis Zhdanov
 * @since 9/19/12 6:41 PM
 */
class KotlinArrangementEntryDependencyInfo(val anchorEntry: KotlinElementArrangementEntry) {

  private val myDependentEntries = ArrayList<KotlinArrangementEntryDependencyInfo>()

  val dependentEntriesInfos: List<KotlinArrangementEntryDependencyInfo>
    get() = myDependentEntries

  fun addDependentEntryInfo(info: KotlinArrangementEntryDependencyInfo) {
    myDependentEntries.add(info)
  }

  override fun toString(): String {
    return anchorEntry.toString()
  }


}

