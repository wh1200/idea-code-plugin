package com.wuhao.code.check.gotohandler

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement

/**
 *
 * Created by 吴昊 on 2019/9/25.
 *
 * @author 吴昊
 * @since
 */
class GotoController : GotoDeclarationHandler {

  override fun getGotoDeclarationTargets(el: PsiElement?, offset: Int, editor: Editor?): Array<PsiElement> {
    return arrayOf()
  }

}
