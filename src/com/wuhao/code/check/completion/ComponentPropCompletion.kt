package com.wuhao.code.check.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlToken
import com.intellij.util.ProcessingContext
import com.wuhao.code.check.allFields
import com.wuhao.code.check.gotohandler.VueHandler
import com.wuhao.code.check.hasDecorator
import com.wuhao.code.check.linemarker.VueLineMarkerProvider.Companion.PROP_ICON_FILE
import com.wuhao.code.check.toDashCase

/**
 * Created by 吴昊 on 2019/2/12.
 */
class ComponentPropCompletion : CompletionContributor() {

  init {
    val provider = ConfigPropertiesCompletionProvider()
    val pattern = PlatformPatterns
        .psiElement(XmlToken::class.java)
        .withParent(PlatformPatterns
            .psiElement(XmlAttribute::class.java)
        )
    extend(CompletionType.BASIC, pattern, provider)
  }

  /**
   *
   * @author 吴昊
   * @since 1.3.16
   */
  inner class ConfigPropertiesCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters,
                                processingContext: ProcessingContext,
                                result: CompletionResultSet) {
      val el = parameters.position.parent as XmlAttribute
      val ref = VueHandler.getRefTSClass(el)
      ref?.allFields?.filter { it.hasDecorator("Prop") }?.forEach {
        val type = if (it is TypeScriptField) {
          it.type?.typeText
        } else {
          null
        }
        result.addElement(PrioritizedLookupElement.withExplicitProximity(
            LookupElementBuilder
                .create(":" + it.name!!.toDashCase().toLowerCase())
                .bold()
                .withIcon(PROP_ICON_FILE)
                .withPresentableText(it.name!!)
                .withTypeText(type)
                .withCaseSensitivity(false),
            10000
        ))
      }
    }

  }

}
