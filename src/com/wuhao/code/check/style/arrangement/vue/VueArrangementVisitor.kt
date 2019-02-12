/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.vue

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClassExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList.ModifierType.GET
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.arrangement.DefaultArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokenType
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.XML_ATTRIBUTE
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.XML_TAG
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.Stack
import com.wuhao.code.check.getChildByType
import com.wuhao.code.check.hasDecorator
import com.wuhao.code.check.hasModifier
import com.wuhao.code.check.inspection.fix.VueComponentPropertySortFix.Companion.LIFE_CYCLE_METHODS
import com.wuhao.code.check.lang.RecursiveVisitor
import com.wuhao.code.check.style.invertible
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import java.util.*

/**
 * vue排序访问器
 * @author 吴昊
 * @since 1.3.1
 */
class VueArrangementVisitor(private val myInfo: VueArrangementParseInfo,
                            private val myRanges: Collection<TextRange>) : VueRecursiveVisitor() {

  private val myCachedClassProperties = ContainerUtil.newHashMap<TypeScriptClassExpression, Set<ES6FieldStatementImpl>>()
  private val myStack = Stack<VueElementArrangementEntry>()

  companion object {
    const val SCRIPT_TAG = "script"
    const val STYLE_TAG = "style"
    const val TEMPLATE_TAG = "template"
    val TS_CLASS = invertible("CLASS", StdArrangementTokenType.ENTRY_TYPE)
    val VUE_COMPUTED = invertible("VUE_COMPUTED", StdArrangementTokenType.ENTRY_TYPE)
    val VUE_DATA_FIELD = invertible("VUE_DATA_FIELD", StdArrangementTokenType.ENTRY_TYPE)
    val VUE_LIFE_HOOK = invertible("VUE_LIFE_HOOK", StdArrangementTokenType.ENTRY_TYPE)
    val VUE_METHOD = invertible("VUE_METHOD", StdArrangementTokenType.ENTRY_TYPE)
    val VUE_MODEL = invertible("VUE_MODEL", StdArrangementTokenType.ENTRY_TYPE)
    val VUE_PROP = invertible("VUE_PROP", StdArrangementTokenType.ENTRY_TYPE)
    val VUE_RENDER = invertible("VUE_RENDER", StdArrangementTokenType.ENTRY_TYPE)
    val VUE_WATCH = invertible("VUE_WATCH", StdArrangementTokenType.ENTRY_TYPE)
  }

  override fun visitElement(element: PsiElement) {
    when (element) {
      is ES6ExportDefaultAssignment -> super.visitElement(element)
      is JSEmbeddedContent          -> super.visitElement(element)
      is TypeScriptClassExpression  -> {
        this.visitTypeScriptClass(element)
      }
      is ES6FieldStatementImpl      -> {
        this.visitField(element)
      }
      is TypeScriptFunction         -> {
        this.visitFunction(element)
      }
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
    val entry = createNewEntry(
        tag.textRange, XML_TAG, null, null, null, true)
    if (tag.name == SCRIPT_TAG) {
      if (tag.getAttribute("lang") != null && tag.getAttributeValue("lang") in listOf("ts", "tsx")) {
        processEntry(entry, tag)
      } else {
        processEntry(entry, null)
      }
    } else {
      processEntry(entry, tag)
    }
  }

  private fun createNewEntry(range: TextRange,
                             type: ArrangementSettingsToken,
                             name: String?,
                             value: String?,
                             namespace: String?,
                             canBeMatched: Boolean): VueElementArrangementEntry? {
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

  private fun getReferencedProperties(property: ES6FieldStatementImpl, tsField: TypeScriptField): List<ES6FieldStatementImpl> {
    val referencedElements = ArrayList<ES6FieldStatementImpl>()
    val propertyInitializer = tsField.initializer
    val containingClass = property.parent as TypeScriptClassExpression
    var classProperties: Set<ES6FieldStatementImpl>? = myCachedClassProperties[containingClass]
    if (classProperties == null) {
      classProperties = containingClass.getChildrenOfType<ES6FieldStatementImpl>().toSet()
      myCachedClassProperties[containingClass] = classProperties
    }
    if (propertyInitializer != null) {
      propertyInitializer.accept(object : RecursiveVisitor() {

        var myCurrentMethodLookupDepth: Int = 0

        override fun visitElement(element: PsiElement) {
          if (element is JSReferenceExpression) {
            this.visitReferenceExpression(element)
          } else {
            element.children.forEach {
              visit(it)
            }
          }
        }

        fun visitReferenceExpression(element: JSReferenceExpression) {
          val el = element.resolve()
          if (el != null && el is TypeScriptField && el.parent in classProperties) {
            referencedElements.add(el.parent as ES6FieldStatementImpl)
          }
        }

      })
    }
    return referencedElements
  }

  private fun isWithinBounds(range: TextRange): Boolean {
    for (textRange in myRanges) {
      if (textRange.intersects(range)) {
        return true
      }
    }
    return false
  }

  private fun processEntry(entry: VueElementArrangementEntry?, nextElement: PsiElement?) {
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

  private fun visitField(element: ES6FieldStatementImpl) {
    val tsField = element.getChildByType<TypeScriptField>()
    val name = if (tsField != null) {
      tsField.name
    } else {
      element.name
    }
    val type = when {
      element.hasDecorator("Model") -> VUE_MODEL
      element.hasDecorator("Prop")  -> VUE_PROP
      else                          -> VUE_DATA_FIELD
    }
    val entry = createNewEntry(
        element.textRange, type, name, null, null, true
    )
    if (entry != null) {
      processEntry(entry, null)
      myInfo.onPropertyEntryCreated(element, entry)
      if (tsField != null) {
        val referencedFields = getReferencedProperties(element, tsField)
        for (referencedField in referencedFields) {
          myInfo.registerPropertyInitializationDependency(element, referencedField)
        }
      }
    }
  }

  private fun visitFunction(element: TypeScriptFunction) {
    val type = when {
      element.hasDecorator("Watch") -> VUE_WATCH
      element.hasModifier(GET)      -> VUE_COMPUTED
      else                          -> when {
        element.name in LIFE_CYCLE_METHODS              -> VUE_LIFE_HOOK
        element.name in listOf("render", "renderError") -> VUE_RENDER
        else                                            -> VUE_METHOD
      }
    }
    val entry = createNewEntry(
        element.textRange, type, element.name, null, null, true
    )
    processEntry(entry, null)
  }

  private fun visitTypeScriptClass(element: TypeScriptClassExpression) {
    val entry = createNewEntry(
        element.textRange, TS_CLASS, element.name, null, null, true
    )
    processEntry(entry, element)
  }

}
