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
import org.jetbrains.kotlin.idea.completion.ItemPriority.SUPER_METHOD_WITH_ARGUMENTS
import org.jetbrains.kotlin.idea.completion.assignPriority

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
      if (ref != null) {
        ref.allFields.filter { it.hasDecorator("Prop") }.forEach {
          val type = if (it is TypeScriptField) {
            it.type?.typeText
          } else {
            null
          }
          result.addElement(LookupElementBuilder.create(it.name!!)
              .bold()
              .withIcon(PROP_ICON_FILE)
              .withTypeText(type)
              .withCaseSensitivity(false)
              .assignPriority(SUPER_METHOD_WITH_ARGUMENTS))
        }
      }
    }

  }

}
