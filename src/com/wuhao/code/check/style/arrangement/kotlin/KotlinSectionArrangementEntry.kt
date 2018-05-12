/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.kotlin

import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.arrangement.ArrangementEntry
import com.intellij.psi.codeStyle.arrangement.TextAwareArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken

/**
 *
 * @author 吴昊
 * @since 1.2.6
 */
class KotlinSectionArrangementEntry(parent: ArrangementEntry?,
                                    type: ArrangementSettingsToken,
                                    range: TextRange,
                                    private val myText: String,
                                    canBeMatched: Boolean) : KotlinElementArrangementEntry(parent, range.startOffset, range.endOffset, type, "SECTION", canBeMatched), TextAwareArrangementEntry {

  override fun getText(): String {
    return myText
  }

}

