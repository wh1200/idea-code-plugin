package com.wuhao.code.check.gotohandler

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.vuejs.VueLanguage
import org.jetbrains.vuejs.language.VueJSLanguage

/**
 * Created by 吴昊 on 2017/7/18.
 * @author 吴昊
 * @since 1.3.8
 */
class VueLifetimeFunctionHandler : GotoDeclarationHandler {

  override fun getActionText(p0: DataContext): String? {
    return "none"
  }

  override fun getGotoDeclarationTargets(el: PsiElement?, p1: Int, p2: Editor?): Array<PsiElement>? {
    if (el != null) {
      println(el.language::class)
      if (el.language is VueLanguage || el.language is VueJSLanguage) {
        if (el is LeafPsiElement && el.parent is TypeScriptFunction) {
          println(el.text)
          println(el.elementType)
          println(el.elementType::class)
        }
      }
    }
    return arrayOf()
  }

}
