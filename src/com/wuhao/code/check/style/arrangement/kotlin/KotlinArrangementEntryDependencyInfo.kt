/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wuhao.code.check.style.arrangement.kotlin

import java.util.*

/**
 * @author Denis Zhdanov
 * @since 9/19/12 6:41 PM
 */
class KotlinArrangementEntryDependencyInfo(val anchorEntry: KotlinElementArrangementEntry) {

  val dependentEntriesInfos: List<KotlinArrangementEntryDependencyInfo>
    get() = myDependentEntries
  private val myDependentEntries = ArrayList<KotlinArrangementEntryDependencyInfo>()

  fun addDependentEntryInfo(info: KotlinArrangementEntryDependencyInfo) {
    myDependentEntries.add(info)
  }

  override fun toString(): String {
    return anchorEntry.toString()
  }

}

