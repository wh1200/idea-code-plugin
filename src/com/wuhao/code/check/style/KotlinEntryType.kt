/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style

import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokenType
import com.intellij.psi.codeStyle.arrangement.std.StdInvertibleArrangementSettingsToken
import com.intellij.util.containers.ContainerUtilRt

object EntryType {
  private val TOKENS = collectFields(EntryType::class.java)
  val CLASS: ArrangementSettingsToken = invertible("CLASS", StdArrangementTokenType.ENTRY_TYPE)
  val CONSTRUCTOR: ArrangementSettingsToken = invertible("CONSTRUCTOR", StdArrangementTokenType.ENTRY_TYPE)
  val ENUM: ArrangementSettingsToken = invertible("ENUM", StdArrangementTokenType.ENTRY_TYPE)
  val EVENT_HANDLER: ArrangementSettingsToken = invertible("EVENT_HANDLER", StdArrangementTokenType.ENTRY_TYPE)
  val INIT_BLOCK: ArrangementSettingsToken = invertible("INITIALIZER_BLOCK", StdArrangementTokenType.ENTRY_TYPE)
  val INTERFACE: ArrangementSettingsToken = invertible("INTERFACE", StdArrangementTokenType.ENTRY_TYPE)
  val FUNCTION: ArrangementSettingsToken = invertible("FUNCTION", StdArrangementTokenType.ENTRY_TYPE)
  val NAMESPACE: ArrangementSettingsToken = invertible("NAMESPACE", StdArrangementTokenType.ENTRY_TYPE)
  val OBJECT: ArrangementSettingsToken = invertible("OBJECT", StdArrangementTokenType.ENTRY_TYPE)
  val COMPANION_OBJECT: ArrangementSettingsToken = invertible("COMPANION_OBJECT", StdArrangementTokenType.ENTRY_TYPE)
  val DATA_CLASS: ArrangementSettingsToken = invertible("DATA_CLASS", StdArrangementTokenType.ENTRY_TYPE)
  val PROPERTY: ArrangementSettingsToken = invertible("PROPERTY", StdArrangementTokenType.ENTRY_TYPE)

  fun values(): Set<ArrangementSettingsToken> {
    return TOKENS.value
  }
}
internal val TOKENS_BY_ID = ContainerUtilRt.newHashMap<String, StdArrangementSettingsToken>()
/**
 *
 * @param id
 * @param type
 * @return
 */
fun invertible(id: String, type: StdArrangementTokenType): StdArrangementSettingsToken {
  val result = StdInvertibleArrangementSettingsToken.invertibleTokenById(id, type)
  TOKENS_BY_ID[id] = result
  return result
}
/**
 *
 * @param clazz
 * @return
 */
fun collectFields(clazz: Class<*>): NotNullLazyValue<Set<ArrangementSettingsToken>> {
  return object : NotNullLazyValue<Set<ArrangementSettingsToken>>() {
    override fun compute(): Set<ArrangementSettingsToken> {
      val result = ContainerUtilRt.newHashSet<ArrangementSettingsToken>()
      for (field in clazz.fields) {
        if (ArrangementSettingsToken::class.java.isAssignableFrom(field.type)) {
          try {
            result.add(field.get(null) as ArrangementSettingsToken)
          } catch (e: IllegalAccessException) {
            assert(false) { e }
          }

        }
      }
      return result
    }
  }
}

