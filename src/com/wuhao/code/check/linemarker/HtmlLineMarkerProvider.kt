/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.linemarker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.lang.javascript.psi.JSNewExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.html.HtmlTag
import com.wuhao.code.check.getChildByType
import com.wuhao.code.check.id
import com.wuhao.code.check.posterity
import icons.VuejsIcons
import javax.swing.Icon

/**
 * 为Mybatis的mapper配置文件提供跳转至对应的Mapper接口类的gutter
 * @author 吴昊
 * @since 1.1
 */
class HtmlLineMarkerProvider : RelatedItemLineMarkerProvider() {

  override fun collectNavigationMarkers(el: PsiElement,
                                        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
    if (el is HtmlTag && !el.id.isNullOrBlank()) {
      val newVueExpressions = el.containingFile.posterity.filterIsInstance<JSNewExpression>().filter {
        it.getChildByType<JSReferenceExpression>()?.text == "Vue"
      }
      newVueExpressions.forEach {
        if (it.arguments.size == 1) {
          val arg = it.arguments[0]
          if (arg is JSObjectLiteralExpression) {
            val property = arg.properties.filter { (it.name in listOf("el", "template")) }
                .firstOrNull { it.value?.text == "'#${el.id}'" }
            if (property != null) {
              result.add(createLineMarkerInfo(el, VuejsIcons.Vue, listOf(property)))
            }
          }
        }
      }
    }
  }

  private fun createLineMarkerInfo(source: PsiElement, file: Icon,
                                   targets: List<PsiElement> = listOf()): RelatedItemLineMarkerInfo<*> {
    return NavigationGutterIconBuilder.create(file).setTargets(targets).createLineMarkerInfo(source)
  }

}
