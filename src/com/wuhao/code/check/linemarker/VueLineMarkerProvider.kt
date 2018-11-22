/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.linemarker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClassExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.getAncestor
import com.wuhao.code.check.getChildOfType
import org.jetbrains.vuejs.VueLanguage
import org.jetbrains.vuejs.language.VueJSLanguage
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
        "destroyed", "errorCaptured", "beforeRouteEnter", "beforeRouteUpdate", "beforeRouteLeave")
    val LIFETIME_ICON_FILE = IconLoader.getIcon("/icons/vue-lifetime.png")
    val PROP_ICON_FILE = IconLoader.getIcon("/icons/in.png")
    val WATCH_ICON_FILE = IconLoader.getIcon("/icons/eye.png")
  }

  override fun collectNavigationMarkers(el: PsiElement,
                                        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
    val lang = el.containingFile.language
    if (lang is VueLanguage || lang is VueJSLanguage) {
      val maybeFunctionIdentifier = el.getAncestor(2) is TypeScriptClassExpression
      val maybePropertyIdentifier = el.getAncestor(3) is TypeScriptClassExpression
      if ((el is LeafPsiElement
              && el.elementType == JSTokenTypes.IDENTIFIER
              && (maybeFunctionIdentifier || maybePropertyIdentifier))) {
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
        } else if (maybePropertyIdentifier && el.parent is TypeScriptField) {
          if (hasAnnotationDecorator(el.getAncestor(3) as TypeScriptClassExpression)) {
            if (hasAnnotation(el.parent, "@Prop")) {
              result.add(createLineMarkerInfo(el, PROP_ICON_FILE))
            }
          }
        }
      }
    }
  }

  private fun createLineMarkerInfo(source: PsiElement, file: Icon): RelatedItemLineMarkerInfo<*> {
    return NavigationGutterIconBuilder.create(file).setTargets(listOf()).createLineMarkerInfo(source)
  }

  private fun hasAnnotation(el: PsiElement, annotation: String): Boolean {
    if (el is TypeScriptField) {
      return hasAnnotation(el.parent, annotation)
    }
    return el.getChildOfType<JSAttributeList>()?.getChildOfType<ES6Decorator>()?.text?.startsWith(annotation)
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
    return el.getChildOfType<JSAttributeList>()?.text?.startsWith(annotation)
        ?: false
  }

}
