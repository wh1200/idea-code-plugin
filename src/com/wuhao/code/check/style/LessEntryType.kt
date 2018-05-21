package com.wuhao.code.check.style

import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokenType

/**
 *
 * @author 吴昊
 * @since 1.4
 */
object LessEntryType {

  val CSS_ELEMENT: ArrangementSettingsToken = invertible("CSS_ELEMENT", StdArrangementTokenType.ENTRY_TYPE)

}
