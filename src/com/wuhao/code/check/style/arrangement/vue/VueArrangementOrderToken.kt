/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement.vue

import com.intellij.psi.codeStyle.arrangement.ArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.CustomArrangementOrderToken
import com.wuhao.code.check.inspection.fix.VueTemplateTagFix
import com.wuhao.code.check.inspection.visitor.BaseCodeFormatVisitor
import java.util.*

/**
 * vue代码重排属性排序器
 * @author 吴昊
 * @since
 */
class VueArrangementOrderToken(id: String, name: String) : CustomArrangementOrderToken(id, name) {

  override fun getEntryComparator(): Comparator<ArrangementEntry> {
    return kotlin.Comparator { e1, e2 ->
      if (e1 is VueElementArrangementEntry
          && e2 is VueElementArrangementEntry) {
        val name1 = e1.name
        val name2 = e2.name
        val nameList = listOf(name1, name2)
        if (e1.value == null && e2.value == null) {
          e1.name.compareTo(e2.name)
        } else if (e1.value == null) {
          -1
        } else if (e2.value == null) {
          1
        } else if (nameList.any { it.startsWith(BaseCodeFormatVisitor.DIRECTIVE_PREFIX) }) {
          VueTemplateTagFix.comparePrefix(nameList, BaseCodeFormatVisitor.DIRECTIVE_PREFIX)
        } else if (nameList.any {
              !it.startsWith(BaseCodeFormatVisitor.CUSTOM_ATTR_PREFIX) && !it.startsWith(BaseCodeFormatVisitor.ACTION_PREFIX)
            }) {
          if (!name1.startsWith(BaseCodeFormatVisitor.CUSTOM_ATTR_PREFIX) && !name1.startsWith(BaseCodeFormatVisitor.ACTION_PREFIX)
              && !name2.startsWith(BaseCodeFormatVisitor.CUSTOM_ATTR_PREFIX) && !name2.startsWith(BaseCodeFormatVisitor.ACTION_PREFIX)) {
            name1.compareTo(name2)
          } else if (!name1.startsWith(BaseCodeFormatVisitor.CUSTOM_ATTR_PREFIX) && !name1.startsWith(BaseCodeFormatVisitor.ACTION_PREFIX)) {
            -1
          } else {
            1
          }
        } else if (nameList.any { it.startsWith(BaseCodeFormatVisitor.CUSTOM_ATTR_PREFIX) }) {
          VueTemplateTagFix.comparePrefix(nameList, BaseCodeFormatVisitor.CUSTOM_ATTR_PREFIX)
        } else if (nameList.any { it.startsWith(BaseCodeFormatVisitor.ACTION_PREFIX) }) {
          VueTemplateTagFix.comparePrefix(nameList, BaseCodeFormatVisitor.ACTION_PREFIX)
        } else {
          0
        }
      } else {
        0
      }
    }
  }
}

