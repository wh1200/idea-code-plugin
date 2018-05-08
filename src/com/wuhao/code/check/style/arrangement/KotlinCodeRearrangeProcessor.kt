/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.arrangement.*
import com.intellij.psi.codeStyle.arrangement.engine.ArrangementEngine
import com.intellij.psi.codeStyle.arrangement.group.ArrangementGroupingRule
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementEntryMatcher
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementMatchRule
import com.intellij.psi.codeStyle.arrangement.model.ArrangementAtomMatchCondition
import com.intellij.psi.codeStyle.arrangement.std.*
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Grouping.*
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.BREADTH_FIRST
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.DEPTH_FIRST
import com.intellij.util.containers.ContainerUtilRt
import com.wuhao.code.check.processors.EntryType.CLASS
import com.wuhao.code.check.processors.EntryType.FIELD
import com.wuhao.code.check.processors.EntryType.INIT_BLOCK
import com.wuhao.code.check.processors.EntryType.INTERFACE
import com.wuhao.code.check.processors.EntryType.METHOD
import java.util.*

/**
 * kotlin代码重排
 * @author 吴昊
 * @since 1.2.7
 */
class KotlinCodeRearrangeProcessor : Rearranger<ArrangementEntry> {

  private val SETTINGS_SERIALIZER = DefaultArrangementSettingsSerializer(getDefaultSettings())

  override fun parseWithNew(
      root: PsiElement, document: Document?,
      ranges: MutableCollection<TextRange>, element: PsiElement,
      settings: ArrangementSettings): Pair<ArrangementEntry, List<ArrangementEntry>>? {
    val existingEntriesInfo = KotlinArrangementParseInfo()
    root.accept(KotlinArrangementVisitor(existingEntriesInfo, document, ranges, settings))

    val newEntryInfo = KotlinArrangementParseInfo()
    element.accept(KotlinArrangementVisitor(newEntryInfo, document, setOf(element.textRange), settings))
    return if (newEntryInfo.entries.size != 1) {
      null
    } else Pair.create<ArrangementEntry, List<ArrangementEntry>>(newEntryInfo
        .entries[0], existingEntriesInfo.entries)

  }

  override fun parse(root: PsiElement, document: Document?,
                     ranges: MutableCollection<TextRange>,
                     settings: ArrangementSettings): List<ArrangementEntry> {
    // Following entries are subject to arrangement: class, interface, field, method.
    val parseInfo = KotlinArrangementParseInfo()
    root.accept(KotlinArrangementVisitor(parseInfo, document, ranges, settings))
    for (rule in settings.groupings) {
      when {
        DEPENDENT_METHODS == rule.groupingType -> setupUtilityMethods(parseInfo, rule.orderType)
        OVERRIDDEN_METHODS == rule.groupingType -> setupOverriddenMethods(parseInfo)
      }
    }
    val fieldDependencyRoots = parseInfo.fieldDependencyRoots
    if (!fieldDependencyRoots.isEmpty()) {
      setupFieldInitializationDependencies(fieldDependencyRoots, settings, parseInfo)
    }
    return parseInfo.entries
  }


  override fun getBlankLines(settings: CodeStyleSettings,
                             parentEntry: ArrangementEntry?,
                             previousEntry: ArrangementEntry?,
                             targetEntry: ArrangementEntry): Int {
    return if (previousEntry == null) {
      -1
    } else {
      val target = targetEntry as KotlinElementArrangementEntry
      val parent = parentEntry as KotlinElementArrangementEntry?
      val previous = previousEntry as KotlinElementArrangementEntry?
      when (target.type) {
        FIELD -> when {
          parent != null && parent.type === INTERFACE -> 2
          previous == null -> 1
          previous.type == INIT_BLOCK -> 1
          previous.type == FIELD -> 0
          else -> 0
        }
        METHOD -> when {
          parent != null && parent.type === INTERFACE -> 1
          previous == null -> 1
          previous.type == FIELD -> 1
          else -> 1
        }
        CLASS -> 1
        INIT_BLOCK -> 1
        else -> -1
      }
    }
  }

  override fun getSerializer(): ArrangementSettingsSerializer {
    return SETTINGS_SERIALIZER
  }

  private fun setupOverriddenMethods(info: KotlinArrangementParseInfo) {
    for (methodsInfo in info.overriddenMethods) {
      var previous: KotlinElementArrangementEntry? = null
      for (entry in methodsInfo.methodEntries) {
        if (previous != null && entry.dependencies == null) {
          entry.addDependency(previous)
        }
        previous = entry
      }
    }
  }

  private fun setupFieldInitializationDependencies(fieldDependencyRoots: List<KotlinArrangementEntryDependencyInfo>,
                                                   settings: ArrangementSettings,
                                                   parseInfo: KotlinArrangementParseInfo) {
    val fields = parseInfo.fields
    val arrangedFields = ArrangementEngine.arrange(fields, settings.sections, settings.rulesSortedByPriority, null)

    for (root in fieldDependencyRoots) {
      val anchorField = root.anchorEntry
      val anchorEntryIndex = arrangedFields.indexOf(anchorField)

      for (fieldInInitializerInfo in root.dependentEntriesInfos) {
        val fieldInInitializer = fieldInInitializerInfo.anchorEntry
        if (arrangedFields.indexOf(fieldInInitializer) > anchorEntryIndex) {
          anchorField.addDependency(fieldInInitializer)
        }
      }
    }
  }

  private fun setupUtilityMethods(info: KotlinArrangementParseInfo, orderType: ArrangementSettingsToken) {
    when {
      DEPTH_FIRST == orderType -> for (rootInfo in info.methodDependencyRoots) {
        setupDepthFirstDependency(rootInfo)
      }
      BREADTH_FIRST == orderType -> for (rootInfo in info.methodDependencyRoots) {
        setupBreadthFirstDependency(rootInfo)
      }
      else -> assert(false) { orderType }
    }
  }

  private fun setupBreadthFirstDependency(info: KotlinArrangementEntryDependencyInfo) {
    val toProcess = ArrayDeque<KotlinArrangementEntryDependencyInfo>()
    toProcess.add(info)
    var prev = info.anchorEntry
    while (!toProcess.isEmpty()) {
      val current = toProcess.removeFirst()
      for (dependencyInfo in current.dependentEntriesInfos) {
        val dependencyMethod = dependencyInfo.anchorEntry
        if (dependencyMethod.dependencies == null) {
          dependencyMethod.addDependency(prev)
          prev = dependencyMethod
        }
        toProcess.addLast(dependencyInfo)
      }
    }
  }

  private fun setupDepthFirstDependency(info: KotlinArrangementEntryDependencyInfo) {
    for (dependencyInfo in info.dependentEntriesInfos) {
      setupDepthFirstDependency(dependencyInfo)
      val dependentEntry = dependencyInfo.anchorEntry
      if (dependentEntry.dependencies == null) {
        dependentEntry.addDependency(info.anchorEntry)
      }
    }
  }

  companion object {
    private fun getDefaultSettings(): StdArrangementSettings {
      val groupingRules = ContainerUtilRt.newArrayList(ArrangementGroupingRule(GETTERS_AND_SETTERS))
      val matchRules = ContainerUtilRt.newArrayList<StdArrangementMatchRule>()
      val aliasTokens = listOf(StdArrangementRuleAliasToken("visibility").apply {
        definitionRules = listOf(StdArrangementTokens.Modifier.PUBLIC, StdArrangementTokens.Modifier.PACKAGE_PRIVATE, StdArrangementTokens.Modifier.PROTECTED, StdArrangementTokens.Modifier.PRIVATE).map {
          StdArrangementMatchRule(
              StdArrangementEntryMatcher(ArrangementAtomMatchCondition(it))
          )
        }
      })
      return StdArrangementExtendableSettings.createByMatchRules(groupingRules, matchRules, aliasTokens)
    }
  }
}

