/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

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
import com.intellij.util.containers.ContainerUtilRt
import com.wuhao.code.check.style.EntryType.CLASS
import com.wuhao.code.check.style.EntryType.FUNCTION
import com.wuhao.code.check.style.EntryType.INIT_BLOCK
import com.wuhao.code.check.style.EntryType.INTERFACE
import com.wuhao.code.check.style.EntryType.PROPERTY

/**
 * kotlin代码重排
 * @author 吴昊
 * @since 1.2.7
 */
class KotlinRearranger : Rearranger<ArrangementEntry> {

  private val settingsSerializer = DefaultArrangementSettingsSerializer(getDefaultSettings())

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
      FUNCTION -> when {
        parent != null && parent.type === INTERFACE -> 1
        previous == null -> 1
        previous.type == PROPERTY -> 1
        else -> 1
      }
      CLASS -> 1
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
    } else Pair.create<ArrangementEntry, List<ArrangementEntry>>(newEntryInfo
        .entries[0], existingEntriesInfo.entries)
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

