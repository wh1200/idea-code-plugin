/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement

import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.BY_NAME
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.KEEP
import com.wuhao.code.check.style.LessEntryType.CSS_ELEMENT
import com.wuhao.code.check.style.LessEntryType.CSS_RULESET

/**
 * vue重排规则列表
 * @author 吴昊
 * @since 1.3.1
 */
object LessRearrangeRules : BaseRules() {

  override fun get(): List<RuleDescription> {
    return listOf(
        RuleDescription(CSS_ELEMENT, BY_NAME),
        RuleDescription(CSS_RULESET, KEEP)
    )
  }

}

