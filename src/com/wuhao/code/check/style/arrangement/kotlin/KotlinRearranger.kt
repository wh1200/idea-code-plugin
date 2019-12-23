/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.kotlin

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.arrangement.*
import com.intellij.psi.codeStyle.arrangement.group.ArrangementGroupingRule
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementEntryMatcher
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementMatchRule
import com.intellij.psi.codeStyle.arrangement.model.ArrangementAtomMatchCondition
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementExtendableSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementRuleAliasToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Grouping.GETTERS_AND_SETTERS
import com.wuhao.code.check.style.KotlinEntryType.CLASS
import com.wuhao.code.check.style.KotlinEntryType.ENUM_ENTRY
import com.wuhao.code.check.style.KotlinEntryType.FUNCTION
import com.wuhao.code.check.style.KotlinEntryType.INIT_BLOCK
import com.wuhao.code.check.style.KotlinEntryType.INTERFACE
import com.wuhao.code.check.style.KotlinEntryType.PROPERTY
import com.wuhao.code.check.style.arrangement.kotlin.KotlinArrangementVisitor.Companion.MAX_METHOD_LOOKUP_DEPTH

/**
 * kotlin代码重排
 * @author 吴昊
 * @since 1.2.7
 */
class KotlinRearranger : Rearranger<ArrangementEntry> {

  private val settingsSerializer = DefaultArrangementSettingsSerializer(getDefaultSettings())

  companion object {
    private fun getDefaultSettings(): StdArrangementSettings {
      val groupingRules = listOf(ArrangementGroupingRule(GETTERS_AND_SETTERS))
      val matchRules = arrayListOf<StdArrangementMatchRule>()
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

  override fun getBlankLines(settings: CodeStyleSettings,
                             parentEntry: ArrangementEntry?,
                             previousEntry: ArrangementEntry?,
                             targetEntry: ArrangementEntry): Int {
    val target = targetEntry as KotlinElementArrangementEntry
    val parent = parentEntry as KotlinElementArrangementEntry?
    val previous = previousEntry as KotlinElementArrangementEntry?
    return when (target.type) {
      PROPERTY -> when {
        parent != null && parent.type === INTERFACE -> 2
        previous == null -> 1
        previous.type == INIT_BLOCK -> 1
        previous.type == PROPERTY -> 0
        else -> 0
      }
      FUNCTION -> 1
      CLASS -> 1
      ENUM_ENTRY -> when {
        previous != null && previous.type == ENUM_ENTRY -> 0
        else -> 1
      }
      INIT_BLOCK -> 1
      else -> -1
    }
  }

  override fun getSerializer(): ArrangementSettingsSerializer {
    return settingsSerializer
  }

  override fun parse(root: PsiElement, document: Document?,
                     ranges: MutableCollection<TextRange>,
                     settings: ArrangementSettings): List<ArrangementEntry> {
    // Following entries are subject to arrangement: class, property, function, interface.
    val parseInfo = KotlinArrangementParseInfo()
    root.accept(KotlinArrangementVisitor(parseInfo, document, ranges, settings))
    val propertyDependencyRoots = parseInfo.getPropertyDependencyRoots()
    if (propertyDependencyRoots.isNotEmpty()) {
      setupPropertyInitializationDependencies(propertyDependencyRoots)
    }
    return parseInfo.entries
  }

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
    } else {
      Pair.create<ArrangementEntry, List<ArrangementEntry>>(newEntryInfo
          .entries[0], existingEntriesInfo.entries)
    }
  }

  private fun setupPropertyInitializationDependencies(propertyDependencyRoots: List<KotlinArrangementEntryDependencyInfo>) {
    val dependencyMap = propertyDependencyRoots.associateBy({ it }, { it.dependentEntriesInfos })
    for (root in propertyDependencyRoots) {
      val anchorProperty = root.anchorEntry
      val dependencyEntries = root.dependentEntriesInfos
      var maxDependencyDepth = MAX_METHOD_LOOKUP_DEPTH
      var tmpEntries: List<KotlinArrangementEntryDependencyInfo>
      while (maxDependencyDepth > 0) {
        tmpEntries = dependencyEntries.toList()
        tmpEntries.forEach { entry ->
          dependencyMap[entry]?.forEach {
            root.addDependentEntryInfo(it)
          }
        }
        maxDependencyDepth--
      }
      for (propertyInInitializerInfo in dependencyEntries) {
        val propertyInInitializer = propertyInInitializerInfo.anchorEntry
        anchorProperty.addDependency(propertyInInitializer)
      }
    }
  }

}

