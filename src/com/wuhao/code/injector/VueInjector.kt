package com.wuhao.code.injector

import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueInjector : LanguageInjector {

  override fun getLanguagesToInject(host: PsiLanguageInjectionHost,
                                    places: InjectedLanguagePlaces) {
    if (host is JSStringTemplateExpression && host.parent is JSProperty
        && (host.parent as JSProperty).name == "template") {
      places.addPlace(VueLanguage.INSTANCE, TextRange.from(1, host.textRange.length - 2), "", "")
    }
  }

}
