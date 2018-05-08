/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement

import com.intellij.openapi.util.Pair
import com.intellij.psi.codeStyle.arrangement.ArrangementEntryDependencyInfo
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.ContainerUtilRt
import com.intellij.util.containers.Stack
import gnu.trove.TObjectIntHashMap
import org.jetbrains.kotlin.psi.KtClass
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
  private val myMethodDependencyRoots = ArrayList<KotlinArrangementEntryDependencyInfo>()
  private val myMethodDependencies = HashMap<KtNamedFunction, MutableSet<KtNamedFunction /* dependencies */>>()
  private val myMethodEntriesMap = HashMap<KtNamedFunction, KotlinElementArrangementEntry>()
  private val myOverriddenMethods = LinkedHashMap<KtClass, MutableList<Pair<KtNamedFunction/*overridden*/,
      KtNamedFunction/*overriding*/>>>()
  private val myTmpMethodDependencyRoots = LinkedHashSet<KtNamedFunction>()
  private val myDependentMethods = HashSet<KtNamedFunction>()
  private val myFields = LinkedHashMap<KtProperty, KotlinElementArrangementEntry>()
  private val myFieldDependencies = HashMap<KtProperty, Set<KtProperty>>()
  private var myRebuildMethodDependencies: Boolean = false

  val entries: List<KotlinElementArrangementEntry>
    get() = myEntries

  /**
   * @return list of method dependency roots, i.e. there is a possible case that particular method
   * [calls another method][ArrangementEntryDependencyInfo.getDependentEntriesInfos], it calls other methods
   * and so forth
   */
  val methodDependencyRoots: List<KotlinArrangementEntryDependencyInfo>
    get() {
      if (myRebuildMethodDependencies) {
        myMethodDependencyRoots.clear()
        val cache = HashMap<KtNamedFunction, KotlinArrangementEntryDependencyInfo>()
        for (method in myTmpMethodDependencyRoots) {
          val info = buildMethodDependencyInfo(method, cache)
          if (info != null) {
            myMethodDependencyRoots.add(info)
          }
        }
        myRebuildMethodDependencies = false
      }
      return myMethodDependencyRoots
    }

  val overriddenMethods: List<KotlinArrangementOverriddenMethodsInfo>
    get() {
      val result = ArrayList<KotlinArrangementOverriddenMethodsInfo>()
      val weights = TObjectIntHashMap<KtNamedFunction>()
      val comparator = Comparator<Pair<KtNamedFunction, KtNamedFunction>> { o1, o2 ->
        weights.get(o1.first) - weights.get(o2
            .first)
      }
      for ((key, value) in myOverriddenMethods) {
        val info = KotlinArrangementOverriddenMethodsInfo(key.name!!)
        weights.clear()
        ContainerUtil.sort(value, comparator)
        for (pair in value) {
          val overridingMethodEntry = myMethodEntriesMap[pair.second]
          if (overridingMethodEntry != null) {
            info.addMethodEntry(overridingMethodEntry)
          }
        }
        if (!info.methodEntries.isEmpty()) {
          result.add(info)
        }
      }

      return result
    }

  val fieldDependencyRoots: List<KotlinArrangementEntryDependencyInfo>
    get() = KotlinFieldDependenciesManager(myFieldDependencies, myFields).roots

  val fields: Collection<KotlinElementArrangementEntry>
    get() = myFields.values

  fun addEntry(entry: KotlinElementArrangementEntry) {
    myEntries.add(entry)
  }


  private fun buildMethodDependencyInfo(
      method: KtNamedFunction,
      cache: MutableMap<KtNamedFunction, KotlinArrangementEntryDependencyInfo>): KotlinArrangementEntryDependencyInfo? {
    val entry = myMethodEntriesMap[method] ?: return null
    val result = KotlinArrangementEntryDependencyInfo(entry)
    val toProcess = Stack<Pair<KtNamedFunction, KotlinArrangementEntryDependencyInfo>>()
    toProcess.push(Pair.create(method, result))
    val usedMethods = ContainerUtilRt.newHashSet<KtNamedFunction>()
    while (!toProcess.isEmpty()) {
      val pair = toProcess.pop()
      val dependentMethods = myMethodDependencies[pair.first] ?: continue
      usedMethods.add(pair.first)
      for (dependentMethod in dependentMethods) {
        if (usedMethods.contains(dependentMethod)) {
          // Prevent cyclic dependencies.
          return null
        }
        val dependentEntry = myMethodEntriesMap[dependentMethod] ?: continue
        val dependentMethodInfo: KotlinArrangementEntryDependencyInfo? = cache[dependentMethod]
        if (dependentMethodInfo == null) {
          cache[dependentMethod] = KotlinArrangementEntryDependencyInfo(dependentEntry)
        }
        val dependentPair = Pair.create<KtNamedFunction, KotlinArrangementEntryDependencyInfo>(dependentMethod,
            dependentMethodInfo)
        pair.second.addDependentEntryInfo(dependentPair.second)
        toProcess.push(dependentPair)
      }
    }
    return result
  }


  fun onMethodEntryCreated(method: KtNamedFunction, entry: KotlinElementArrangementEntry) {
    myMethodEntriesMap[method] = entry
  }

  fun onFieldEntryCreated(field: KtProperty, entry: KotlinElementArrangementEntry) {
    myFields[field] = entry
  }

  /**
   * Is expected to be called when new method dependency is detected. Here given `'base method'` calls
   * `'dependent method'`.
   */
  fun registerMethodCallDependency(caller: KtNamedFunction, callee: KtNamedFunction) {
    myTmpMethodDependencyRoots.remove(callee)
    if (!myDependentMethods.contains(caller)) {
      myTmpMethodDependencyRoots.add(caller)
    }
    myDependentMethods.add(callee)
    val methods: MutableSet<KtNamedFunction>? = myMethodDependencies[caller]
    if (methods == null) {
      myMethodDependencies[caller] = LinkedHashSet()
    }
    if (!methods!!.contains(callee)) {
      methods.add(callee)
    }
    myRebuildMethodDependencies = true
  }
}

