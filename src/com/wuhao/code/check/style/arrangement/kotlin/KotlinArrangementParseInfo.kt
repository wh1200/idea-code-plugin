/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.kotlin

import com.intellij.util.containers.ContainerUtil
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * @author Denis Zhdanov
 * @since 9/18/12 11:11 AM
 */
class KotlinArrangementParseInfo {

  val entries: List<KotlinElementArrangementEntry>
    get() = myEntries
  private val myEntries = ArrayList<KotlinElementArrangementEntry>()
  private val myFields = LinkedHashMap<KtProperty, KotlinElementArrangementEntry>()
  private val myMethodEntriesMap = HashMap<KtNamedFunction, KotlinElementArrangementEntry>()
  private val myFieldDependencies = ContainerUtil.newHashMap<KtProperty, HashSet<KtProperty>>()

  fun addEntry(entry: KotlinElementArrangementEntry) {
    myEntries.add(entry)
  }

  fun onFieldEntryCreated(field: KtProperty, entry: KotlinElementArrangementEntry) {
    myFields[field] = entry
  }

  fun onMethodEntryCreated(method: KtNamedFunction, entry: KotlinElementArrangementEntry) {
    myMethodEntriesMap[method] = entry
  }

  fun registerFieldInitializationDependency(property: KtProperty, referencedField: KtProperty) {
    var fields: MutableSet<KtProperty>? = myFieldDependencies[property]
    if (fields == null) {
      fields = ContainerUtil.newHashSet()
      myFieldDependencies[property] = fields
    }
    fields.add(referencedField)
  }

  fun getFields(): Collection<KotlinElementArrangementEntry> {
    return myFields.values
  }

  fun getFieldDependencyRoots(): List<KotlinArrangementEntryDependencyInfo> {
    return PropertyDependenciesManager(myFieldDependencies, myFields).roots
  }

}

