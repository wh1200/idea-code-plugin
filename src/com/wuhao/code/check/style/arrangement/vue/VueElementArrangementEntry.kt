/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.vue

import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.arrangement.*
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken

/**
 *
 * @author 吴昊
 * @since 1.3.1
 */
class VueElementArrangementEntry(parent: ArrangementEntry?,
                                 range: TextRange,
                                 val type: ArrangementSettingsToken,
                                 private val myName: String,
                                 val value: String?,
                                 val myNamespace: String?,
                                 canBeMatched: Boolean)
  : DefaultArrangementEntry(parent, range.startOffset, range.endOffset, canBeMatched), TypeAwareArrangementEntry, NameAwareArrangementEntry, NamespaceAwareArrangementEntry {

  private val myTypes = hashSetOf(type)

  override fun getName(): String {
    return myName
  }

  override fun getTypes(): MutableSet<ArrangementSettingsToken> {
    return myTypes
  }

  override fun getNamespace(): String? {
    return myNamespace
  }

}

