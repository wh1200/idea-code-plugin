package com.wuhao.code.check.style.arrangement.less

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

/**
 *
 * @author 吴昊
 * @since 1.4
 */
class LessRearranger : Rearranger<ArrangementEntry> {

  private val settingsSerializer = DefaultArrangementSettingsSerializer(getDefaultSettings())

  companion object {
    private fun getDefaultSettings(): StdArrangementSettings {
      val groupingRules = listOf<ArrangementGroupingRule>()
      val matchRules = ArrayList<StdArrangementMatchRule>()
      val aliasTokens = listOf<StdArrangementRuleAliasToken>()
      return StdArrangementExtendableSettings.createByMatchRules(groupingRules, matchRules, aliasTokens)
    }
  }

  override fun getBlankLines(settings: CodeStyleSettings, parent: ArrangementEntry?, previous: ArrangementEntry?, target: ArrangementEntry): Int {
    return -1
  }

  override fun getSerializer(): ArrangementSettingsSerializer {
    return settingsSerializer
  }

  override fun parse(root: PsiElement, document: Document?,
                     ranges: MutableCollection<out TextRange>, settings:
  ArrangementSettings): List<ArrangementEntry> {
    val parseInfo = LessArrangementParseInfo()
    root.accept(LessArrangementVisitor(parseInfo, ranges))
    return parseInfo.entries
  }

  override fun parseWithNew(root: PsiElement, document: Document?,
                            ranges: MutableCollection<out TextRange>,
                            element: PsiElement,
                            settings: ArrangementSettings): Pair<ArrangementEntry, List<ArrangementEntry>>? {
    val existingEntriesInfo = LessArrangementParseInfo()
    root.accept(LessArrangementVisitor(existingEntriesInfo, ranges))
    val newEntryInfo = LessArrangementParseInfo()
    element.accept(LessArrangementVisitor(newEntryInfo, setOf(element.textRange)))
    return if (newEntryInfo.entries.size != 1) {
      null
    } else {
      Pair.create<ArrangementEntry, List<ArrangementEntry>>(newEntryInfo
          .entries[0], existingEntriesInfo.entries)
    }
  }

}

