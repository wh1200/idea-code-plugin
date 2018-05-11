/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.gotohandler

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry

/**
 * Created by 吴昊 on 2017/7/18.
 * @author 吴昊
 * @since 1.0
 */
class MyGotoDeclarationHandler2 : GotoDeclarationHandler {

  override fun getActionText(p0: DataContext?): String? {
    return null
  }

  override fun getGotoDeclarationTargets(el: PsiElement?, p1: Int, p2: Editor?): Array<PsiElement>? {
    val res = arrayListOf<PsiElement>()
    val parent = el?.parent
    if (parent != null && (parent is KtLiteralStringTemplateEntry || parent is PsiLiteralExpression)) {

    }
    return res.toTypedArray()
  }

}

