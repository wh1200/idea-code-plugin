/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style

import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokenType
import com.intellij.psi.codeStyle.arrangement.std.StdInvertibleArrangementSettingsToken

/**
 * kotlin排序类型
 * @author 吴昊
 * @since
 */
object KotlinEntryType {

  val CLASS: ArrangementSettingsToken = invertible("CLASS", StdArrangementTokenType.ENTRY_TYPE)
  val COMPANION_OBJECT: ArrangementSettingsToken = invertible("COMPANION_OBJECT", StdArrangementTokenType.ENTRY_TYPE)
  val CONSTRUCTOR: ArrangementSettingsToken = invertible("CONSTRUCTOR", StdArrangementTokenType.ENTRY_TYPE)
  val DATA_CLASS: ArrangementSettingsToken = invertible("DATA_CLASS", StdArrangementTokenType.ENTRY_TYPE)
  val ENUM: ArrangementSettingsToken = invertible("ENUM", StdArrangementTokenType.ENTRY_TYPE)
  val ENUM_ENTRY: ArrangementSettingsToken = invertible("ENUM_ENTRY", StdArrangementTokenType.ENTRY_TYPE)
  val EVENT_HANDLER: ArrangementSettingsToken = invertible("EVENT_HANDLER", StdArrangementTokenType.ENTRY_TYPE)
  val FUNCTION: ArrangementSettingsToken = invertible("FUNCTION", StdArrangementTokenType.ENTRY_TYPE)
  val INIT_BLOCK: ArrangementSettingsToken = invertible("INITIALIZER_BLOCK", StdArrangementTokenType.ENTRY_TYPE)
  val INTERFACE: ArrangementSettingsToken = invertible("INTERFACE", StdArrangementTokenType.ENTRY_TYPE)
  val NAMESPACE: ArrangementSettingsToken = invertible("NAMESPACE", StdArrangementTokenType.ENTRY_TYPE)
  val OBJECT: ArrangementSettingsToken = invertible("OBJECT", StdArrangementTokenType.ENTRY_TYPE)
  val PROPERTY: ArrangementSettingsToken = invertible("PROPERTY", StdArrangementTokenType.ENTRY_TYPE)
  val SECONDARY_CONSTRUCTOR: ArrangementSettingsToken = invertible("SECONDARY_CONSTRUCTOR", StdArrangementTokenType.ENTRY_TYPE)

}

/**
 * token id到token的映射map
 */
internal val TOKENS_BY_ID = HashMap<String, StdArrangementSettingsToken>()

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
inline fun <reified T> collectFields(clazz: Class<*>): HashSet<T> {
  val result = HashSet<T>()
  for (field in clazz.declaredFields) {
    if (field.type == T::class.java) {
      try {
        result.add(field.get(null) as T)
      } catch (e: Throwable) {
        assert(false) { e }
      }
    }
  }
  return result
}

