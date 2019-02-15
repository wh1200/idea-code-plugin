/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement

import com.intellij.psi.codeStyle.arrangement.ArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.CustomArrangementOrderToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.XML_ATTRIBUTE
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.XML_TAG
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.VUE_COMPUTED
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.VUE_DATA_FIELD
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.VUE_FILE_ROOT_TAG
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.VUE_LIFE_HOOK
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.VUE_METHOD
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.VUE_MODEL
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.VUE_PROP
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.VUE_RENDER
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor.Companion.VUE_WATCH
import com.wuhao.code.check.style.arrangement.vue.VueElementArrangementEntry
import com.wuhao.code.check.style.arrangement.vue.VueLifeHookOrderToken
import com.wuhao.code.check.style.arrangement.vue.VueTemplateAttrOrderToken

/**
 * vue重排规则列表
 * @author 吴昊
 * @since 1.3.1
 */
object VueRearrangeRules : BaseRules() {

  override fun get(): List<RuleDescription> {
    val vueAttrOrderToken = VueTemplateAttrOrderToken("BY_VUE_ATTR", "arrangement.settings.order.type.by.vue.attr")
    val vueLifeHookOrderToken = VueLifeHookOrderToken("BY_VUE_LIFE_HOOK",
        "arrangement.settings.order.type.by.vue.life.hook")
    return listOf(
        RuleDescription(XML_ATTRIBUTE, vueAttrOrderToken),
        RuleDescription(VUE_FILE_ROOT_TAG, VueRootTagOrderToken()),
        RuleDescription(XML_TAG, Order.KEEP),
        RuleDescription(VUE_MODEL, Order.BY_NAME),
        RuleDescription(VUE_PROP, Order.BY_NAME),
        RuleDescription(VUE_DATA_FIELD, Order.BY_NAME),
        RuleDescription(VUE_COMPUTED, Order.BY_NAME),
        RuleDescription(VUE_WATCH, Order.BY_NAME),
        RuleDescription(VUE_LIFE_HOOK, vueLifeHookOrderToken),
        RuleDescription(VUE_METHOD, Order.BY_NAME),
        RuleDescription(VUE_RENDER, Order.BY_NAME)
    )
  }

}

/**
 * vue文件根标签排序器
 * @author 吴昊
 * @since 1.4.7
 */
class VueRootTagOrderToken() : CustomArrangementOrderToken("BY_VUE_ROOT_TAG",
    "arrangement.settings.order.type.by.vue.root.tag") {

  private val rootTags = arrayOf("template", "script", "style")

  override fun getEntryComparator(): Comparator<ArrangementEntry> {
    return Comparator { e1, e2 ->
      if (e1 is VueElementArrangementEntry
          && e2 is VueElementArrangementEntry) {
        rootTags.indexOf(e1.name) - rootTags.indexOf(e2.name)
      } else {
        0
      }
    }
  }

}
