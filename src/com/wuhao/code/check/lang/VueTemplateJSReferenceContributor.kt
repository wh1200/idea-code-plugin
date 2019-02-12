package com.wuhao.code.check.lang

import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.wuhao.code.check.allFields
import com.wuhao.code.check.allFunctions
import com.wuhao.code.check.gotohandler.VueHandler.Companion.findTypeScriptClass

/**
 *
 * Created by 吴昊 on 2019/2/12.
 *
 * @author 吴昊
 * @since 1.4.6
 */
class VueTemplateJSReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(
        JSReferenceExpression::class.java
    ), object : PsiReferenceProvider() {

      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        if (element.containingFile.name.endsWith(".vue")) {
          val tsClass = findTypeScriptClass(element.containingFile)
          if (tsClass != null) {
            val list: List<PsiNameIdentifierOwner> = tsClass.allFields.toList() + tsClass.allFunctions.toList()
            val el = list.firstOrNull { it.name == element.text }
            if (el != null) {
              return arrayOf(VueTemplateReference(element, el))
            }
          }
        }
        return arrayOf()
      }

    })
  }

}
