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
import com.wuhao.code.check.vueEnabled
import org.jetbrains.vuejs.lang.html.VueLanguage

/**
 * 预处理标签属性，对v-bind,v-on等进行缩写
 * @author 吴昊
 * @since 1.3.1
 */
class VueAttrFixPreProcessor : PreFormatProcessor {

  companion object {
    val TAGS = listOf(
        "!DOCTYPE", "a", "abbr", "acronym", "address",
        "applet", "area", "article", "aside", "audio", "b", "base",
        "basefont", "bdi", "bdo", "big", "blockquote", "body", "br",
        "button", "canvas", "caption", "center", "cite", "code", "col",
        "colgroup", "command", "datalist", "dd", "del", "details",
        "dir", "div", "dfn", "dialog", "dl", "dt", "em", "embed",
        "fieldset", "figcaption", "figure", "font", "footer", "form",
        "frame", "frameset", "h1toh6", "head", "header", "hr", "html",
        "i", "iframe", "img", "input", "ins", "isindex", "kbd", "keygen",
        "label", "legend", "li", "link", "map", "mark", "menu", "menuitem",
        "meta", "meter", "nav", "noframes", "noscript", "object", "ol",
        "optgroup", "option", "output", "p", "param", "pre", "progress",
        "q", "rp", "rt", "ruby", "s", "samp", "script", "section", "select",
        "small", "source", "span", "strike", "strong", "style", "sub",
        "summary", "sup", "table", "tbody", "td", "textarea", "tfoot",
        "th", "thead", "time", "title", "tr", "track", "tt", "u", "ul",
        "var", "video", "wbr", "xmp", "template", "keep-alive",
        "component", "slot", "transaction", "transaction-group"
    )
  }

  override fun process(astNode: ASTNode, textRange: TextRange): TextRange {
    if (!vueEnabled || astNode.psi.language != VueLanguage.INSTANCE) {
      return textRange
    }
    astNode.psi.accept(
        object : VueRecursiveVisitor() {

          override fun visitXmlAttribute(attribute: XmlAttribute) {
            if (!attribute.name.startsWith(BIND) && !attribute.name.startsWith(ON)) {
              return
            }
            val factory = XmlElementFactory.getInstance(attribute.project)
            if (attribute.value == null) {
              return
            }
            val newAttr = factory.createAttribute(
                VueDirectives.getShortName(attribute.name),
                attribute.value!!,
                attribute.parent
            )
            attribute.replace(newAttr)
            super.visitXmlAttribute(attribute)
          }

        }
    )
    return textRange
  }

}
