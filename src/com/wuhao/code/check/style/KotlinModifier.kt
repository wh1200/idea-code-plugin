/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style

import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.CompositeArrangementToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokenType
import com.wuhao.code.check.style.KotlinEntryType.FUNCTION

/**
 * kotlin的排序修饰符
 * @author 吴昊
 * @since 1.3.1
 */
object KotlinModifier {

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

  fun values() = collectFields<ArrangementSettingsToken>(KotlinModifier::class.java)

  private fun compositeToken(id: String,
                             type: StdArrangementTokenType,
                             vararg alternativeTokens: ArrangementSettingsToken): StdArrangementSettingsToken {
    val result = CompositeArrangementToken.create(id, "function modifiers", type, *alternativeTokens)
    TOKENS_BY_ID[id] = result
    return result
  }

}

