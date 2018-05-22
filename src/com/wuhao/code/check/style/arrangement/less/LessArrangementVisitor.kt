/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.less

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.arrangement.DefaultArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.psi.css.CssDeclaration
import com.intellij.psi.css.CssRuleset
import com.intellij.util.containers.Stack
import com.wuhao.code.check.style.LessEntryType.CSS_ELEMENT
import com.wuhao.code.check.style.LessEntryType.CSS_RULESET

/**
 * vue排序访问器
 * @author 吴昊
 * @since 1.3.1
 */
class LessArrangementVisitor(private val myInfo: LessArrangementParseInfo,
                             private val myRanges: Collection<TextRange>) : LessRecursiveVisitor() {

  private val myStack = Stack<LessElementArrangementEntry>()

  override fun visitCssDeclaration(declaration: CssDeclaration) {
    val entry = createNewEntry(
        declaration.textRange,
        CSS_ELEMENT,
        declaration.name,
        true
    )
    processEntry(entry, null)
  }

  override fun visitCssRuleset(ruleset: CssRuleset) {
    val entry = createNewEntry(
        ruleset.textRange, CSS_RULESET, ruleset.presentableText, true)
    processEntry(entry, ruleset)
  }

  private fun createNewEntry(range: TextRange,
                             type: ArrangementSettingsToken,
                             name: String?,
                             canBeMatched: Boolean): LessElementArrangementEntry? {
    if (range.startOffset == 0 && range.endOffset == 0 || !isWithinBounds(range)) {
      return null
    }
    val current = getCurrent()
    val entry = LessElementArrangementEntry(
        current, range, type, name ?: "", canBeMatched)
    if (current == null) {
      myInfo.addEntry(entry)
    } else {
      current.addChild(entry)
    }
    return entry
  }

  private fun getCurrent(): DefaultArrangementEntry? {
    return if (myStack.isEmpty()) {
      null
    } else {
      myStack.peek()
    }
  }

  private fun isWithinBounds(range: TextRange): Boolean {
    for (textRange in myRanges) {
      if (textRange.intersects(range)) {
        return true
      }
    }
    return false
  }

  private fun processEntry(entry: LessElementArrangementEntry?, nextElement: PsiElement?) {
    if (entry == null || nextElement == null) {
      return
    }
    myStack.push(entry)
    try {
      nextElement.acceptChildren(this)
    } finally {
      myStack.pop()
    }
  }

}

