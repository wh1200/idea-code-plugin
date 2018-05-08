/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.processors

import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.arrangement.std.*
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.ContainerUtilRt
import com.wuhao.code.check.LanguageNames
import com.wuhao.code.check.RecursiveVisitor
import com.wuhao.code.check.inspection.fix.VueTemplateTagFix
import com.wuhao.code.check.processors.EntryType.METHOD
import org.jetbrains.kotlin.psi.psiUtil.endOffset

/**
 * 格式化代码时自动对vue模板中的标签属性进行排序
 * @author 吴昊
 * @since 1.1
 */
class FixVueAttributesProcessor : PostFormatProcessor {

  override fun processElement(el: PsiElement, styleSettings: CodeStyleSettings): PsiElement {
    if (el.language.displayName == LanguageNames.vue) {
      if (el is XmlTag) {
        VueTemplateTagFix.fixElement(el)
      }
    }
    return el
  }

  override fun processText(file: PsiFile, textRange: TextRange, styleSettings: CodeStyleSettings): TextRange {
    if (file is HtmlFileImpl && file.language.displayName == LanguageNames.vue) {
      val templateTag = file.document?.children?.firstOrNull { it is XmlTag && it.name == "template" }
      if (templateTag != null) {
        object : RecursiveVisitor() {
          override fun visitElement(element: PsiElement) {
            if (element is XmlTag) {
              VueTemplateTagFix.fixWhitespace(element)
            }
          }
        }.visit(templateTag)
      }
    }
    return TextRange(0, file.endOffset)
  }

}

object EntryType {
  val CLASS: ArrangementSettingsToken = invertible("CLASS", StdArrangementTokenType.ENTRY_TYPE)
  val FIELD: ArrangementSettingsToken = invertible("FIELD", StdArrangementTokenType.ENTRY_TYPE)
  val CONSTRUCTOR: ArrangementSettingsToken = invertible("CONSTRUCTOR", StdArrangementTokenType.ENTRY_TYPE)
  val METHOD: ArrangementSettingsToken = invertible("METHOD", StdArrangementTokenType.ENTRY_TYPE)
  val ENUM: ArrangementSettingsToken = invertible("ENUM", StdArrangementTokenType.ENTRY_TYPE)
  val INTERFACE: ArrangementSettingsToken = invertible("INTERFACE", StdArrangementTokenType.ENTRY_TYPE)
  val PROPERTY: ArrangementSettingsToken = invertible("PROPERTY", StdArrangementTokenType.ENTRY_TYPE)
  val EVENT_HANDLER: ArrangementSettingsToken = invertible("EVENT_HANDLER", StdArrangementTokenType.ENTRY_TYPE)
  val STATIC_INIT: ArrangementSettingsToken = invertible("STATIC_INIT", StdArrangementTokenType.ENTRY_TYPE)
  val INIT_BLOCK: ArrangementSettingsToken = invertible("INITIALIZER_BLOCK", StdArrangementTokenType.ENTRY_TYPE)
  val NAMESPACE: ArrangementSettingsToken = invertible("NAMESPACE", StdArrangementTokenType.ENTRY_TYPE)

  private val TOKENS = collectFields(EntryType::class.java)

  fun values(): Set<ArrangementSettingsToken> {
    return TOKENS.value
  }
}

object Modifier {


  val PUBLIC: ArrangementSettingsToken = invertible("PUBLIC", StdArrangementTokenType.MODIFIER)
  val PROTECTED: ArrangementSettingsToken = invertible("PROTECTED", StdArrangementTokenType.MODIFIER)
  val PRIVATE: ArrangementSettingsToken = invertible("PRIVATE", StdArrangementTokenType.MODIFIER)
  val OPEN: ArrangementSettingsToken = invertible("OPEN", StdArrangementTokenType.MODIFIER)
  val LATEINIT: ArrangementSettingsToken = invertible("LATEINIT", StdArrangementTokenType.MODIFIER)
  val INTERNAL: ArrangementSettingsToken = invertible("INTERNAL", StdArrangementTokenType.MODIFIER)
  val INLINE: ArrangementSettingsToken = invertible("INLINE", StdArrangementTokenType.MODIFIER)
  val PACKAGE_PRIVATE: ArrangementSettingsToken = invertible("PACKAGE_PRIVATE", StdArrangementTokenType.MODIFIER)
  val STATIC: ArrangementSettingsToken = invertible("STATIC", StdArrangementTokenType.MODIFIER)
  val FINAL: ArrangementSettingsToken = invertible("FINAL", StdArrangementTokenType.MODIFIER)
  val SEALED: ArrangementSettingsToken = invertible("SEALED", StdArrangementTokenType.MODIFIER)
  val READONLY: ArrangementSettingsToken = invertible("READONLY", StdArrangementTokenType.MODIFIER)
  val VOLATILE: ArrangementSettingsToken = invertible("VOLATILE", StdArrangementTokenType.MODIFIER)
  val SYNCHRONIZED: ArrangementSettingsToken = invertible("SYNCHRONIZED", StdArrangementTokenType.MODIFIER)
  val CONST: ArrangementSettingsToken = invertible("CONST", StdArrangementTokenType.ENTRY_TYPE)
  val EXTERNAL: ArrangementSettingsToken = invertible("EXTERNAL", StdArrangementTokenType.ENTRY_TYPE)
  val ABSTRACT: ArrangementSettingsToken = invertible("ABSTRACT", StdArrangementTokenType.MODIFIER)
  val OVERRIDE: ArrangementSettingsToken = invertible("OVERRIDE", StdArrangementTokenType.MODIFIER)
  val GETTER: ArrangementSettingsToken = compositeToken("GETTER", StdArrangementTokenType.MODIFIER, METHOD, PUBLIC)
  val SETTER: ArrangementSettingsToken = compositeToken("SETTER", StdArrangementTokenType.MODIFIER, METHOD, PUBLIC)
  val OVERRIDDEN: ArrangementSettingsToken = compositeToken("OVERRIDDEN", StdArrangementTokenType.MODIFIER, METHOD, PUBLIC, PROTECTED)
  private val TOKENS = collectFields(Modifier::class.java)

  val MODIFIER_AS_TYPE: Set<ArrangementSettingsToken> = ContainerUtil.newHashSet(GETTER, SETTER, OVERRIDDEN)

  fun values(): Set<ArrangementSettingsToken> {
    return TOKENS.value
  }


  private fun compositeToken(id: String,
                             type: StdArrangementTokenType,
                             vararg alternativeTokens: ArrangementSettingsToken): StdArrangementSettingsToken {
    val result = CompositeArrangementToken.create(id, type, *alternativeTokens)
    TOKENS_BY_ID.put(id, result)
    return result
  }
}
private val TOKENS_BY_ID = ContainerUtilRt.newHashMap<String, StdArrangementSettingsToken>()
/**
 *
 * @param id
 * @param type
 * @return
 */
fun invertible(id: String, type: StdArrangementTokenType): StdArrangementSettingsToken {
  val result = StdInvertibleArrangementSettingsToken.invertibleTokenById(id, type)
  TOKENS_BY_ID[id] = result
  return result
}

private fun collectFields(clazz: Class<*>): NotNullLazyValue<Set<ArrangementSettingsToken>> {
  return object : NotNullLazyValue<Set<ArrangementSettingsToken>>() {
    override fun compute(): Set<ArrangementSettingsToken> {
      val result = ContainerUtilRt.newHashSet<ArrangementSettingsToken>()
      for (field in clazz.fields) {
        if (ArrangementSettingsToken::class.java.isAssignableFrom(field.type)) {
          try {
            result.add(field.get(null) as ArrangementSettingsToken)
          } catch (e: IllegalAccessException) {
            assert(false) { e }
          }

        }
      }
      return result
    }
  }
}

