/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.less

import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.arrangement.ArrangementEntry
import com.intellij.psi.codeStyle.arrangement.DefaultArrangementEntry
import com.intellij.psi.codeStyle.arrangement.NameAwareArrangementEntry
import com.intellij.psi.codeStyle.arrangement.TypeAwareArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken

/**
 *
 * @author 吴昊
 * @since 1.3.1
 */
class LessElementArrangementEntry(parent: ArrangementEntry?,
                                  range: TextRange,
                                  val type: ArrangementSettingsToken,
                                  private val myName: String,
                                  canBeMatched: Boolean)
  : DefaultArrangementEntry(parent, range.startOffset, range.endOffset, canBeMatched),
    TypeAwareArrangementEntry, NameAwareArrangementEntry {

  private val myTypes = hashSetOf(type)

  override fun getName(): String {
    return myName
  }

  override fun getTypes(): MutableSet<ArrangementSettingsToken> {
    return myTypes
  }

}

