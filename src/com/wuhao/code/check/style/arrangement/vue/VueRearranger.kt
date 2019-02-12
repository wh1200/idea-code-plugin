/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.vue

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.arrangement.*
import com.intellij.psi.codeStyle.arrangement.group.ArrangementGroupingRule
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementMatchRule
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementExtendableSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementRuleAliasToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.XML_ATTRIBUTE
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.XML_TAG
import com.wuhao.code.check.style.arrangement.kotlin.KotlinArrangementVisitor

/**
 * vue代码排序器，主要对vue的模板标签属性进行排序
 * @author 吴昊
 * @since 1.2.0
 */
class VueRearranger : Rearranger<ArrangementEntry> {

  private val settingsSerializer = DefaultArrangementSettingsSerializer(getDefaultSettings())

  companion object {
    private fun getDefaultSettings(): StdArrangementSettings {
      val groupingRules = listOf<ArrangementGroupingRule>()
      val matchRules = ArrayList<StdArrangementMatchRule>()
      val aliasTokens = listOf<StdArrangementRuleAliasToken>()
      return StdArrangementExtendableSettings.createByMatchRules(groupingRules, matchRules, aliasTokens)
    }
  }

  override fun getBlankLines(settings: CodeStyleSettings,
                             parentEntry: ArrangementEntry?,
                             previousEntry: ArrangementEntry?,
                             targetEntry: ArrangementEntry): Int {
    val previous = previousEntry as VueElementArrangementEntry?
    val target = targetEntry as VueElementArrangementEntry
    return when (target.type) {
      XML_ATTRIBUTE -> {
        if (previous?.value == null) {
          -1
        } else {
          0
        }
      }
      XML_TAG -> -1
      else -> -1
    }
  }

  override fun getSerializer(): ArrangementSettingsSerializer {
    return settingsSerializer
  }

  override fun parse(root: PsiElement, document: Document?,
                     ranges: MutableCollection<TextRange>,
                     settings: ArrangementSettings): List<ArrangementEntry> {
    val parseInfo = VueArrangementParseInfo()
    root.accept(VueArrangementVisitor(parseInfo, ranges))
    val propertyDependencyRoots = parseInfo.getPropertyDependencyRoots()
    if (!propertyDependencyRoots.isEmpty()) {
      setupPropertyInitializationDependencies(propertyDependencyRoots)
    }
    return parseInfo.entries
  }

  override fun parseWithNew(
      root: PsiElement, document: Document?,
      ranges: MutableCollection<TextRange>, element: PsiElement,
      settings: ArrangementSettings): Pair<ArrangementEntry, List<ArrangementEntry>>? {
    val existingEntriesInfo = VueArrangementParseInfo()
    root.accept(VueArrangementVisitor(existingEntriesInfo, ranges))
    val newEntryInfo = VueArrangementParseInfo()
    element.accept(VueArrangementVisitor(newEntryInfo, setOf(element.textRange)))
    return if (newEntryInfo.entries.size != 1) {
      null
    } else {
      Pair.create<ArrangementEntry, List<ArrangementEntry>>(newEntryInfo.entries[0], existingEntriesInfo.entries)
    }
  }

  private fun setupPropertyInitializationDependencies(propertyDependencyRoots: List<VueArrangementEntryDependencyInfo>) {
    val dependencyMap = propertyDependencyRoots.associateBy({ it }, { it.dependentEntriesInfos })
    for (root in propertyDependencyRoots) {
      val anchorProperty = root.anchorEntry
      val dependencyEntries = root.dependentEntriesInfos
      var maxDependencyDepth = KotlinArrangementVisitor.MAX_METHOD_LOOKUP_DEPTH
      var tmpEntries: List<VueArrangementEntryDependencyInfo>
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

