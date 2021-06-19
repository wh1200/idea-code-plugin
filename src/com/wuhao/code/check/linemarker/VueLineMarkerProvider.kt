/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.linemarker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSNewExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClassExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.*
import org.jetbrains.vuejs.VuejsIcons
import javax.swing.Icon

/**
 * 为Mybatis的mapper配置文件提供跳转至对应的Mapper接口类的gutter
 * @author 吴昊
 * @since 1.1
 */
class VueLineMarkerProvider : RelatedItemLineMarkerProvider() {

  companion object {
    val COMPUTED_ICON_FILE = IconLoader.getIcon("/icons/calculator.png")
    val LIFETIME_FUNCTIONS = listOf(
        "beforeCreate", "created", "beforeMount", "mounted",
        "beforeUpdate", "updated", "activated", "deactivated", "beforeDestroy",
        "destroyed", "errorCaptured", "beforeRouteEnter", "beforeRouteUpdate", "beforeRouteLeave"
    )
    val LIFETIME_ICON_FILE = IconLoader.getIcon("/icons/vue-lifetime.png")
    val PROP_ICON_FILE = IconLoader.getIcon("/icons/in.png")
    val WATCH_ICON_FILE = IconLoader.getIcon("/icons/eye.png")
  }

  override fun collectNavigationMarkers(
      el: PsiElement,
      result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
  ) {
    if (PsiPatterns2.newVuePattern().accepts(el)) {
      if (el is JSNewExpression) {
        val targets = arrayListOf<PsiElement>()
        if (el.arguments.size == 1) {
          val arg = el.arguments[0]
          if (arg is JSObjectLiteralExpression) {
            val properties = arg.properties.filter { (it.name in listOf("el", "template")) }
            properties.forEach { property ->
              targets.addAll(el.containingFile.posterity.filter {
                it is HtmlTag && property.value?.text ==
                    "'#${it.id}'"
              })
            }

          }
        }
        result.add(createLineMarkerInfo(el, VuejsIcons.Vue, targets))
      }
    } else if (PsiPatterns2.vueLangPattern().accepts(el)) {
      val maybeFunctionIdentifier = el.getAncestor(2) is TypeScriptClassExpression
      val maybePropertyIdentifier = el.getAncestor(3) is TypeScriptClassExpression
      if ((el is LeafPsiElement
              && el.elementType == JSTokenTypes.IDENTIFIER
              && (maybeFunctionIdentifier || maybePropertyIdentifier))
      ) {
        if (maybeFunctionIdentifier && el.parent is TypeScriptFunction) {
          if (hasAnnotationDecorator(el.getAncestor(2) as TypeScriptClassExpression)) {
            if (el.text in LIFETIME_FUNCTIONS) {
              result.add(createLineMarkerInfo(el, LIFETIME_ICON_FILE))
            }
            if (hasAnnotation(el.parent, "@Watch")) {
              result.add(createLineMarkerInfo(el, WATCH_ICON_FILE))
            }

            if (hasAttribute(el.parent, "get")) {
              result.add(createLineMarkerInfo(el, COMPUTED_ICON_FILE))
            }
          }
        } else if (maybePropertyIdentifier && el.parent is TypeScriptField
            && hasAnnotationDecorator(el.getAncestor(3) as TypeScriptClassExpression)
            && hasAnnotation(el.parent, "@Prop")
        ) {
          result.add(createLineMarkerInfo(el, PROP_ICON_FILE))
        }
      }
    }
  }

  private fun createLineMarkerInfo(
      source: PsiElement, file: Icon,
      targets: List<PsiElement> = listOf()
  ): RelatedItemLineMarkerInfo<*> {
    return NavigationGutterIconBuilder.create(file).setTargets(targets).createLineMarkerInfo(source)
  }

  private fun hasAnnotation(el: PsiElement, annotation: String): Boolean {
    if (el is TypeScriptField) {
      return hasAnnotation(el.parent, annotation)
    }
    return el.getChildByType<JSAttributeList>()?.getChildByType<ES6Decorator>()?.text?.startsWith(annotation)
      ?: false
  }

  private fun hasAnnotationDecorator(ancestor: TypeScriptClassExpression): Boolean {
    val parent = ancestor.parent
    if (parent is ES6ExportDefaultAssignment) {
      return hasAnnotation(parent, "@Component")
    }
    return false
  }

  private fun hasAttribute(el: PsiElement, annotation: String): Boolean {
    return el.getChildByType<JSAttributeList>()?.text?.startsWith(annotation)
      ?: false
  }

}
