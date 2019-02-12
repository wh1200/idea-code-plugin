/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.vue

import com.intellij.psi.codeStyle.arrangement.ArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.CustomArrangementOrderToken
import com.wuhao.code.check.inspection.fix.VueComponentPropertySortFix.Companion.LIFE_CYCLE_METHODS

/**
 * vue代码重排属性排序器
 * @author 吴昊
 * @since 1.2.0
 */
class VueLifeHookOrderToken(id: String, name: String) : CustomArrangementOrderToken(id, name) {

  override fun getEntryComparator(): Comparator<ArrangementEntry> {
    return Comparator { e1, e2 ->
      if (e1 is VueElementArrangementEntry
          && e2 is VueElementArrangementEntry) {
        LIFE_CYCLE_METHODS.indexOf(e1.name) - LIFE_CYCLE_METHODS.indexOf(e2.name)
      } else {
        0
      }
    }
  }

}
