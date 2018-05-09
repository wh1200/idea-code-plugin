package com.wuhao.code.check.processors

import com.intellij.psi.codeStyle.arrangement.std.*
import com.wuhao.code.check.PostStart
import com.wuhao.code.check.processors.EntryType.CLASS
import com.wuhao.code.check.processors.EntryType.CONSTRUCTOR
import com.wuhao.code.check.processors.EntryType.DATA_CLASS
import com.wuhao.code.check.processors.EntryType.FUNCTION
import com.wuhao.code.check.processors.EntryType.INTERFACE
import com.wuhao.code.check.processors.EntryType.OBJECT
import com.wuhao.code.check.processors.EntryType.PROPERTY

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

  /**
   *
   * @return
   */
  fun getKotlinRules(): List<PostStart.RuleDescription> {
    return listOf(
        PostStart.RuleDescription(listOf(PROPERTY, OPEN), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, OPEN, INTERNAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, OPEN, PROTECTED), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, OPEN, PRIVATE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, OPEN, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, OPEN, INTERNAL, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, OPEN, PROTECTED, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, OPEN, PRIVATE, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, INTERNAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, PROTECTED), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, PRIVATE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, INTERNAL, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, PROTECTED, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(PROPERTY, PRIVATE, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(CONSTRUCTOR), StdArrangementTokens.Order.BY_NAME),

        PostStart.RuleDescription(listOf(FUNCTION, OPEN), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, OPEN, INTERNAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, OPEN, PROTECTED), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, OPEN, PRIVATE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, OPEN, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, OPEN, INTERNAL, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, OPEN, PROTECTED, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, OPEN, PRIVATE, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, INTERNAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, PROTECTED), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, PRIVATE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, INTERNAL, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, PROTECTED, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(FUNCTION, PRIVATE, LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(INNER), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(INTERFACE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(OBJECT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(DATA_CLASS), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(CLASS), StdArrangementTokens.Order.BY_NAME))
  }

  fun values(): Set<ArrangementSettingsToken> {
    return TOKENS.value
  }
}

