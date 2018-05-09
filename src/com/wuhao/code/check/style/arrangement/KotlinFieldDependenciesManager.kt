/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement

import com.intellij.util.containers.ContainerUtil
import org.jetbrains.kotlin.psi.KtProperty

/**
 * @author 吴昊
 * @since 1.2.6
 */
class KotlinFieldDependenciesManager(
    private val myFieldDependencies: Map<KtProperty, Set<KtProperty>>,
    fields: Map<KtProperty, KotlinElementArrangementEntry>) {
  private val myFieldInfosMap = ContainerUtil.newHashMap<KtProperty, KotlinArrangementEntryDependencyInfo>()

  val roots: List<KotlinArrangementEntryDependencyInfo>
    get() {
      val list = ContainerUtil.newArrayList<KotlinArrangementEntryDependencyInfo>()

      for ((key, value) in myFieldDependencies) {
        val currentInfo = myFieldInfosMap[key]

        for (usedInInitialization in value) {
          val fieldInfo = myFieldInfosMap[usedInInitialization]
          if (fieldInfo != null) {
            currentInfo?.addDependentEntryInfo(fieldInfo)
          }
        }
        list.add(currentInfo)
      }

      return list
    }

  init {
    for (field in fields.keys) {
      val entry = fields[field]
      myFieldInfosMap[field] = KotlinArrangementEntryDependencyInfo(entry!!)
    }
  }


}

