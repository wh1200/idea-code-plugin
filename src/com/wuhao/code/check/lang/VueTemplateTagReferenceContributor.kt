package com.wuhao.code.check.lang

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.wuhao.code.check.gotohandler.VueHandler

/**
 *
 * Created by 吴昊 on 2019/2/12.
 *
 * @author 吴昊
 * @since 1.4.6
 */
class VueTemplateTagReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(
        XmlTag::class.java), object : PsiReferenceProvider() {

      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext):
          Array<out PsiReference> {
        element as XmlTag
        val refEl = VueHandler.resolveReferenceOfVueComponentTag(element)
        if (refEl != null) {
          return arrayOf(VueTemplateReference(element, refEl))
        } else {
          return arrayOf()
        }
      }

    })
  }

}
