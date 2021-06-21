/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

import com.intellij.util.containers.ContainerUtil
import org.jetbrains.kotlin.psi.KtProperty

/**
 * @author 吴昊
 * @since 1.3.5
 */
class PropertyDependenciesManager(private val myFieldDependencies: Map<KtProperty, Set<KtProperty>>,
                                  fields: Map<KtProperty, KotlinElementArrangementEntry>) {

  val roots: List<KotlinArrangementEntryDependencyInfo>
    get() {
      val list = ArrayList<KotlinArrangementEntryDependencyInfo>()

      for ((key, value) in myFieldDependencies) {
        val currentInfo = myFieldInfosMap[key]

        for (usedInInitialization in value) {
          val fieldInfo = myFieldInfosMap[usedInInitialization]
          if (fieldInfo != null && currentInfo != null) {
            currentInfo.addDependentEntryInfo(fieldInfo)
          }
        }

        list.add(currentInfo!!)
      }

      return list
    }
  private val myFieldInfosMap = HashMap<KtProperty, KotlinArrangementEntryDependencyInfo>()

  init {
    for (field in fields.keys) {
      val entry = fields[field]
      if (entry != null) {
        myFieldInfosMap[field] = KotlinArrangementEntryDependencyInfo(entry)
      }
    }
  }

}

