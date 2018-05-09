/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style

import com.intellij.psi.codeStyle.arrangement.std.*
import com.wuhao.code.check.PostStart
import com.wuhao.code.check.style.EntryType.CLASS
import com.wuhao.code.check.style.EntryType.CONSTRUCTOR
import com.wuhao.code.check.style.EntryType.DATA_CLASS
import com.wuhao.code.check.style.EntryType.FUNCTION
import com.wuhao.code.check.style.EntryType.INTERFACE
import com.wuhao.code.check.style.EntryType.OBJECT
import com.wuhao.code.check.style.EntryType.PROPERTY

object KotlinModifier {


  private val TOKENS = collectFields(KotlinModifier::class.java)
  val ABSTRACT: ArrangementSettingsToken = invertible("ABSTRACT", StdArrangementTokenType.MODIFIER)
  val CONST: ArrangementSettingsToken = invertible("CONST", StdArrangementTokenType.ENTRY_TYPE)
  val EXTERNAL: ArrangementSettingsToken = invertible("EXTERNAL", StdArrangementTokenType.ENTRY_TYPE)
  val FINAL: ArrangementSettingsToken = compositeToken("FINAL", StdArrangementTokenType.MODIFIER, FUNCTION)
  val INLINE: ArrangementSettingsToken = invertible("INLINE", StdArrangementTokenType.MODIFIER)
  val INNER: ArrangementSettingsToken = invertible("INNER", StdArrangementTokenType.MODIFIER)
  val INTERNAL: ArrangementSettingsToken = invertible("INTERNAL", StdArrangementTokenType.MODIFIER)
  val LATEINIT: ArrangementSettingsToken = invertible("LATEINIT", StdArrangementTokenType.MODIFIER)
  val OPEN: ArrangementSettingsToken = invertible("OPEN", StdArrangementTokenType.MODIFIER)
  val PACKAGE_PRIVATE: ArrangementSettingsToken = invertible("PACKAGE_PRIVATE", StdArrangementTokenType.MODIFIER)
  val PRIVATE: ArrangementSettingsToken = invertible("PRIVATE", StdArrangementTokenType.MODIFIER)
  val PROTECTED: ArrangementSettingsToken = invertible("PROTECTED", StdArrangementTokenType.MODIFIER)
  val PUBLIC: ArrangementSettingsToken = invertible("PUBLIC", StdArrangementTokenType.MODIFIER)
  val SEALED: ArrangementSettingsToken = invertible("SEALED", StdArrangementTokenType.MODIFIER)

  private fun compositeToken(id: String,
                             type: StdArrangementTokenType,
                             vararg alternativeTokens: ArrangementSettingsToken): StdArrangementSettingsToken {
    val result = CompositeArrangementToken.create(id, type, *alternativeTokens)
    TOKENS_BY_ID[id] = result
    return result
  }

  fun values(): Set<ArrangementSettingsToken> {
    return TOKENS.value
  }
}

