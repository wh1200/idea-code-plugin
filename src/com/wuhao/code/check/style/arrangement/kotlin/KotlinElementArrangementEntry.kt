/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.kotlin

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.codeStyle.arrangement.*
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.util.containers.ContainerUtilRt

/**
 * Not thread-safe.
 *
 * @author Denis Zhdanov
 * @since 7/20/12 4:50 PM
 */
open class KotlinElementArrangementEntry(parent: ArrangementEntry?,
                                         startOffset: Int,
                                         endOffset: Int,
                                         val type: ArrangementSettingsToken,
                                         private val myName: String?,
                                         canBeArranged: Boolean)
  : DefaultArrangementEntry(parent, startOffset, endOffset, canBeArranged),
    TypeAwareArrangementEntry,
    NameAwareArrangementEntry,
    ModifierAwareArrangementEntry {

  private val myModifiers = ContainerUtilRt.newHashSet<ArrangementSettingsToken>()
  private val myTypes = ContainerUtilRt.newHashSet<ArrangementSettingsToken>()

  constructor(parent: ArrangementEntry?,
              range: TextRange,
              type: ArrangementSettingsToken,
              name: String?,
              canBeMatched: Boolean) : this(parent, range.startOffset, range.endOffset, type, name, canBeMatched)

  init {
    myTypes.add(type)
  }

  override fun getModifiers(): Set<ArrangementSettingsToken> {
    return myModifiers
  }

  override fun getName(): String? {
    return myName
  }

  override fun getTypes(): Set<ArrangementSettingsToken> {
    return myTypes
  }

  override fun toString(): String {
    return String.format(
        "[%d; %d): %s %s %s",
        startOffset, endOffset, StringUtil.join(myModifiers, " ").toLowerCase(),
        myTypes.iterator().next().toString().toLowerCase(), myName ?: "<no name>"
    )
  }

  fun addModifier(modifier: ArrangementSettingsToken) {
    myModifiers.add(modifier)
  }
}

