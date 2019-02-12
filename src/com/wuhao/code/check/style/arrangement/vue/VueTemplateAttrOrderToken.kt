/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.vue

import com.intellij.psi.codeStyle.arrangement.ArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.CustomArrangementOrderToken
import com.wuhao.code.check.inspection.fix.vue.VueTemplateTagFix
import com.wuhao.code.check.inspection.visitor.CommonCodeFormatVisitor.Companion.ACTION_PREFIX
import com.wuhao.code.check.inspection.visitor.CommonCodeFormatVisitor.Companion.CUSTOM_ATTR_PREFIX
import com.wuhao.code.check.inspection.visitor.CommonCodeFormatVisitor.Companion.DIRECTIVE_PREFIX

/**
 * vue代码重排属性排序器
 * @author 吴昊
 * @since 1.2.0
 */
class VueTemplateAttrOrderToken(id: String, name: String) : CustomArrangementOrderToken(id, name) {

  override fun getEntryComparator(): Comparator<ArrangementEntry> {
    return Comparator { e1, e2 ->
      if (e1 is VueElementArrangementEntry
          && e2 is VueElementArrangementEntry) {
        val name1 = e1.name
        val name2 = e2.name
        val nameList = listOf(name1, name2)
        when {
          e1.value == null && e2.value == null               -> e1.name.compareTo(e2.name)
          e1.value == null                                   -> -1
          e2.value == null                                   -> 1
          nameList.any { it.startsWith(DIRECTIVE_PREFIX) }   -> VueTemplateTagFix.comparePrefix(nameList, DIRECTIVE_PREFIX)
          nameList.any {
            !it.startsWith(CUSTOM_ATTR_PREFIX)
                && !it.startsWith(ACTION_PREFIX)
          }                                                  -> when {
            !name1.startsWith(CUSTOM_ATTR_PREFIX) && !name1.startsWith(ACTION_PREFIX)
                && !name2.startsWith(CUSTOM_ATTR_PREFIX)
                && !name2.startsWith(ACTION_PREFIX) -> name1.compareTo(name2)
            !name1.startsWith(CUSTOM_ATTR_PREFIX)
                && !name1.startsWith(ACTION_PREFIX) -> -1
            else                                    -> 1
          }
          nameList.any { it.startsWith(CUSTOM_ATTR_PREFIX) } -> VueTemplateTagFix.comparePrefix(nameList, CUSTOM_ATTR_PREFIX)
          nameList.any { it.startsWith(ACTION_PREFIX) }      -> VueTemplateTagFix.comparePrefix(nameList, ACTION_PREFIX)
          else                                               -> 0
        }
      } else {
        0
      }
    }
  }

}
