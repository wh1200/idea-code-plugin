/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.vue

import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl
import com.intellij.util.containers.ContainerUtil
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
  private val myProperties = LinkedHashMap<ES6FieldStatementImpl, VueElementArrangementEntry>()
  private val myPropertyDependencies = ContainerUtil.newHashMap<ES6FieldStatementImpl, HashSet<ES6FieldStatementImpl>>()

  fun addEntry(entry: VueElementArrangementEntry) {
    myEntries.add(entry)
  }

  fun getPropertyDependencyRoots(): List<VueArrangementEntryDependencyInfo> {
    return PropertyDependenciesManager(myPropertyDependencies, myProperties).roots
  }

  fun onPropertyEntryCreated(property: ES6FieldStatementImpl, entry: VueElementArrangementEntry) {
    myProperties[property] = entry
  }

  fun registerPropertyInitializationDependency(property: ES6FieldStatementImpl, referencedProperty: ES6FieldStatementImpl) {
    var properties: MutableSet<ES6FieldStatementImpl>? = myPropertyDependencies[property]
    if (properties == null) {
      properties = ContainerUtil.newHashSet()
      myPropertyDependencies[property] = properties
    }
    properties.add(referencedProperty)
  }

}
