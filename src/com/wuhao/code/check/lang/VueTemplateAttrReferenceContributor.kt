package com.wuhao.code.check.lang

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext
import com.wuhao.code.check.gotohandler.VueHandler.Companion.getRefTSClass
import com.wuhao.code.check.hasDecorator

/**
 *
 * Created by 吴昊 on 2019/2/12.
 *
 * @author 吴昊
 * @since 1.4.6
 */
class VueTemplateAttrReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(XmlAttribute::class.java), object : PsiReferenceProvider() {

      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        element as XmlAttribute
        val tsClass = getRefTSClass(element)
        if (tsClass != null) {
          val field = tsClass.fields.filter {
            it.hasDecorator("Prop")
          }.firstOrNull { it.name == element.name }
          if (field != null) {
            return arrayOf(VueTemplateReference(element, field))
          }
        }
        return arrayOf()
      }

    })
  }

}

