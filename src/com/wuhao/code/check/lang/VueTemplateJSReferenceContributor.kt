package com.wuhao.code.check.lang

import com.intellij.lang.ecmascript6.psi.ES6ImportCall
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.resolve.JSModuleReferenceContributor
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.wuhao.code.check.PsiPatterns.VUE_FILE
import com.wuhao.code.check.allFields
import com.wuhao.code.check.allFunctions
import com.wuhao.code.check.gotohandler.VueHandler.Companion.findTSClass
import com.wuhao.code.check.hasDecorator

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
        JSLiteralExpression::class.java
    ).withParent(ES6ImportCall::class.java), object : PsiReferenceProvider() {

      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        element as JSLiteralExpression
        val resolvedReferences = JSModuleReferenceContributor.getReferencesForStringLiteral(element, this, false)
        val vueFile = resolvedReferences.map {
          it.resolve()
        }.firstOrNull { it is PsiFile && it.name.endsWith(".vue") }
        if (vueFile is PsiFile) {
          val tsClass = findTSClass(vueFile)
          if (tsClass != null) {
            val name = tsClass.name
            val nameEl = tsClass.nameIdentifier
            if (name != null && nameEl != null) {
              return arrayOf(ResolvedReference(element, tsClass.parent, name, nameEl))
            }
          }
        }
        return element.references
      }

    })
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(
        JSReferenceExpression::class.java
    ).inFile(VUE_FILE), object : PsiReferenceProvider() {

      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        println(element.text)
        val tsClass = findTSClass(element.containingFile)
        if (tsClass != null) {
          val list: List<PsiNameIdentifierOwner> = tsClass.allFields.toList() + tsClass.allFunctions
              .filter { !it.hasDecorator("Watch") }
              .toList()
          val el = list.firstOrNull { it.name == element.text }
          if (el != null) {
            return arrayOf(VueTemplateReference(element, el))
          }
        }
        return arrayOf()
      }

    })
  }

}
