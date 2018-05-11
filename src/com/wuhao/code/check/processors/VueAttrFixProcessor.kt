/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.processors

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.psi.xml.XmlAttribute
import com.wuhao.code.check.lang.vue.VueDirectives
import com.wuhao.code.check.lang.vue.VueDirectives.BIND
import com.wuhao.code.check.lang.vue.VueDirectives.ON
import com.wuhao.code.check.style.arrangement.vue.VueRecursiveVisitor
import org.jetbrains.vuejs.VueLanguage

/**
 * 预处理标签属性，对v-bind,v-on等进行缩写
 * @author 吴昊
 * @since 1.3.1
 */
class VueAttrFixProcessor : PreFormatProcessor {

  override fun process(astNode: ASTNode, textRange: TextRange): TextRange {
    if (astNode.psi.language == VueLanguage.INSTANCE) {
      astNode.psi.accept(
          object : VueRecursiveVisitor() {
            override fun visitXmlAttribute(attribute: XmlAttribute) {
              if (attribute.name.startsWith(BIND) || attribute.name.startsWith(ON)) {
                val factory = XmlElementFactory.getInstance(attribute.project)
                if (attribute.value != null) {
                  val newAttr = factory.createAttribute(VueDirectives.getShortName(attribute.name), attribute.value!!, attribute.parent)
                  attribute.replace(newAttr)
                }
              }
              super.visitXmlAttribute(attribute)
            }
          }
      )
    }
    return textRange
  }

}

