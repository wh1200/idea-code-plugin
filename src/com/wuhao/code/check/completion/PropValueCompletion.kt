package com.wuhao.code.check.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList.ModifierType.GET
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext
import com.wuhao.code.check.*
import com.wuhao.code.check.gotohandler.VueHandler.Companion.findTSClass
import com.wuhao.code.check.linemarker.VueLineMarkerProvider.Companion.COMPUTED_ICON_FILE
import com.wuhao.code.check.linemarker.VueLineMarkerProvider.Companion.LIFETIME_FUNCTIONS
import com.wuhao.code.check.linemarker.VueLineMarkerProvider.Companion.PROP_ICON_FILE
import org.jetbrains.kotlin.idea.completion.ItemPriority.SUPER_METHOD_WITH_ARGUMENTS
import org.jetbrains.kotlin.idea.completion.assignPriority
import org.jetbrains.vuejs.VueLanguage

/**
 * Created by 吴昊 on 2019/2/12.
 */
class PropValueCompletion : CompletionContributor() {

  init {
    val provider = ConfigPropertiesCompletionProvider()
    val pattern = PlatformPatterns.psiElement()
        .inFile(PlatformPatterns.psiFile().withLanguage(VueLanguage.INSTANCE))
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
      val el = parameters.position
      val attr = el.getAncestorOfType<XmlAttribute>()
      if (attr != null && (attr.value.isNullOrBlank() || attr.value == "IntellijIdeaRulezzz ")) {
        val tsClass = findTSClass(attr.containingFile)
        if (tsClass != null) {
          tsClass.allFields.forEach {
            result.addElement(LookupElementBuilder.create(it.name!!)
                .bold()
                .withIcon(when {
                  it.hasDecorator("Prop") -> PROP_ICON_FILE
                  else                    -> null
                })
                .withTypeText(if (it is TypeScriptField) {
                  it.type?.typeText
                } else {
                  null
                })
                .withCaseSensitivity(false)
                .assignPriority(SUPER_METHOD_WITH_ARGUMENTS))
          }
          tsClass.allFunctions
              .filter {
                !it.hasDecorator("Watch") && it.name!! !in LIFETIME_FUNCTIONS
              }
              .forEach {
                result.addElement(LookupElementBuilder.create(it.name!!)
                    .bold()
                    .withIcon(when {
                      it.hasModifier(GET) -> COMPUTED_ICON_FILE
                      else                -> null
                    })
                    .withTypeText("function")
                    .withCaseSensitivity(false)
                    .assignPriority(SUPER_METHOD_WITH_ARGUMENTS))
              }
        }
      }
    }

  }

}
