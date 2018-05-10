/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement

import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.XML_ATTRIBUTE
import com.wuhao.code.check.style.arrangement.vue.VueArrangementOrderToken

/**
 * vue重排规则列表
 * @author 吴昊
 * @since 1.3.1
 */
object VueRearrangeRules : BaseRules() {

  override fun get(): List<RuleDescription> {
    val vueAttrOrderToken = VueArrangementOrderToken("BY_VUE_ATTR", "arrangement.settings.order.type.by.vue.attr")
    return listOf(
        RuleDescription(XML_ATTRIBUTE, vueAttrOrderToken)
    )
  }

}

