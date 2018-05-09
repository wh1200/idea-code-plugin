/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement.vue

import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.arrangement.ArrangementEntry
import com.intellij.psi.codeStyle.arrangement.DefaultArrangementEntry
import com.intellij.psi.codeStyle.arrangement.NameAwareArrangementEntry
import com.intellij.psi.codeStyle.arrangement.TypeAwareArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.util.containers.ContainerUtilRt

/**
 *
 * @author 吴昊
 * @since 1.3.1
 */
class VueElementArrangementEntry(parent: ArrangementEntry?,
                                 startOffset: Int,
                                 endOffset: Int,
                                 val type: ArrangementSettingsToken,
                                 private val myName: String?,
                                 canBeArranged: Boolean)
  : DefaultArrangementEntry(parent, startOffset, endOffset, canBeArranged),
    TypeAwareArrangementEntry,
    NameAwareArrangementEntry {

  constructor(parent: ArrangementEntry?,
              range: TextRange,
              type: ArrangementSettingsToken,
              name: String?,
              canBeMatched: Boolean) : this(parent, range.startOffset, range.endOffset, type, name, canBeMatched)

  private val myTypes = ContainerUtilRt.newHashSet<ArrangementSettingsToken>()

  override fun getName(): String? {
    return myName
  }

  override fun getTypes(): MutableSet<ArrangementSettingsToken> {
    return myTypes
  }

  init {
    myTypes.add(type)
  }

}

