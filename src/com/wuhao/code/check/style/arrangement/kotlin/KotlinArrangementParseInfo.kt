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
  private val myFunctionEntriesMap = HashMap<KtNamedFunction, KotlinElementArrangementEntry>()
  private val myProperties = LinkedHashMap<KtProperty, KotlinElementArrangementEntry>()
  private val myPropertyDependencies = ContainerUtil.newHashMap<KtProperty, HashSet<KtProperty>>()

  fun addEntry(entry: KotlinElementArrangementEntry) {
    myEntries.add(entry)
  }

  fun getProperties(): Collection<KotlinElementArrangementEntry> {
    return myProperties.values
  }

  fun getPropertyDependencyRoots(): List<KotlinArrangementEntryDependencyInfo> {
    return PropertyDependenciesManager(myPropertyDependencies, myProperties).roots
  }

  fun onMethodEntryCreated(method: KtNamedFunction, entry: KotlinElementArrangementEntry) {
    myFunctionEntriesMap[method] = entry
  }

  fun onPropertyEntryCreated(property: KtProperty, entry: KotlinElementArrangementEntry) {
    myProperties[property] = entry
  }

  fun registerPropertyInitializationDependency(property: KtProperty, referencedProperty: KtProperty) {
    var properties: MutableSet<KtProperty>? = myPropertyDependencies[property]
    if (properties == null) {
      properties = ContainerUtil.newHashSet()
      myPropertyDependencies[property] = properties
    }
    properties.add(referencedProperty)
  }

}

