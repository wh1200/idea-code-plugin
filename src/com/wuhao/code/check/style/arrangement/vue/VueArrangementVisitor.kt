/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.vue

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.arrangement.DefaultArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.XML_ATTRIBUTE
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.XML_TAG
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.Stack
import com.intellij.xml.arrangement.XmlElementArrangementEntry

/**
 * vue排序访问器
 * @author 吴昊
 * @since 1.3.1
 */
class VueArrangementVisitor(private val myInfo: VueArrangementParseInfo,
                            private val myRanges: Collection<TextRange>) : VueRecursiveVisitor() {

  private val myStack = Stack<XmlElementArrangementEntry>()

  companion object {
    const val SCRIPT_TAG = "script"
    const val STYLE_TAG = "style"
    const val TEMPLATE_TAG = "template"
  }

  override fun visitElement(element: PsiElement) {
    if (element is ES6ExportDefaultAssignment) {
      super.visitElement(element)
    }
  }


  override fun visitFile(file: PsiFile?) {
    if (file is XmlFile) {
      file.rootTag?.accept(this)
    }
  }


  override fun visitXmlAttribute(attribute: XmlAttribute) {
    val entry = createNewEntry(
        attribute.textRange, XML_ATTRIBUTE, attribute.name, attribute.value, attribute.namespace, true)
    processEntry(entry, null)
  }


  override fun visitXmlTag(tag: XmlTag) {
    if (tag.name != SCRIPT_TAG && tag.name != STYLE_TAG) {
      val entry = createNewEntry(
          tag.textRange, XML_TAG, null, null, null, true)
      processEntry(entry, tag)
    } else {

    }
  }


  private fun createNewEntry(range: TextRange,
                             type: ArrangementSettingsToken,
                             name: String?,
                             value: String?,
                             namespace: String?,
                             canBeMatched: Boolean): XmlElementArrangementEntry? {
    if (range.startOffset == 0 && range.endOffset == 0 || !isWithinBounds(range)) {
      return null
    }
    val current = getCurrent()
    val entry = VueElementArrangementEntry(
        current, range, type, name ?: "", value, namespace, canBeMatched)
    if (current == null) {
      myInfo.addEntry(entry)
    } else {
      current.addChild(entry)
    }
    return entry
  }


  private fun getCurrent(): DefaultArrangementEntry? {
    return if (myStack.isEmpty()) {
      null
    } else {
      myStack.peek()
    }
  }


  private fun isWithinBounds(range: TextRange): Boolean {
    for (textRange in myRanges) {
      if (textRange.intersects(range)) {
        return true
      }
    }
    return false
  }


  private fun processEntry(entry: XmlElementArrangementEntry?, nextElement: PsiElement?) {
    if (entry == null || nextElement == null) {
      return
    }
    myStack.push(entry)
    try {
      nextElement.acceptChildren(this)
    } finally {
      myStack.pop()
    }
  }

}

