/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.kotlin

import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * @author Denis Zhdanov
 * @since 9/18/12 11:11 AM
 */
class KotlinArrangementParseInfo {

  private val myEntries = ArrayList<KotlinElementArrangementEntry>()
  private val myFields = LinkedHashMap<KtProperty, KotlinElementArrangementEntry>()
  private val myMethodEntriesMap = HashMap<KtNamedFunction, KotlinElementArrangementEntry>()
  val entries: List<KotlinElementArrangementEntry>
    get() = myEntries

  fun addEntry(entry: KotlinElementArrangementEntry) {
    myEntries.add(entry)
  }

  fun onFieldEntryCreated(field: KtProperty, entry: KotlinElementArrangementEntry) {
    myFields[field] = entry
  }

  fun onMethodEntryCreated(method: KtNamedFunction, entry: KotlinElementArrangementEntry) {
    myMethodEntriesMap[method] = entry
  }
}

