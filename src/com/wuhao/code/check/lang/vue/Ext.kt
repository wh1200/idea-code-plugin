/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.lang.vue

import com.intellij.psi.xml.XmlAttribute
import com.wuhao.code.check.inspection.visitor.CommonCodeFormatVisitor

/**
 * 是否是有js注入的属性
 * @param attribute
 */
fun isInjectAttribute(attribute: XmlAttribute): Boolean {
  return attribute.name.startsWith(CommonCodeFormatVisitor.CUSTOM_ATTR_PREFIX)
      || attribute.name.startsWith(CommonCodeFormatVisitor.DIRECTIVE_PREFIX)
      || attribute.name.startsWith(CommonCodeFormatVisitor.ACTION_PREFIX)
}

