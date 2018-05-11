/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.vue

import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.arrangement.ArrangementEntry
import com.intellij.psi.codeStyle.arrangement.NameAwareArrangementEntry
import com.intellij.psi.codeStyle.arrangement.TypeAwareArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.xml.arrangement.XmlElementArrangementEntry

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
                                 namespace: String?,
                                 canBeMatched: Boolean)
  : XmlElementArrangementEntry(parent, range, type, myName, namespace, canBeMatched),
    TypeAwareArrangementEntry,
    NameAwareArrangementEntry {

  private val myTypes = hashSetOf(type)

  override fun getName(): String {
    return myName
  }

  override fun getTypes(): MutableSet<ArrangementSettingsToken> {
    return myTypes
  }

}

