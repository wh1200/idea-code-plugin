package com.wuhao.code.check.gotohandler

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClassExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.wuhao.code.check.*
import com.wuhao.code.check.style.arrangement.vue.VueArrangementVisitor
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.vuejs.VueLanguage
import org.jetbrains.vuejs.language.VueJSLanguage

/**
 * Created by 吴昊 on 2017/7/18.
 * @author 吴昊
 * @since 1.3.8
 */
class VueHandler : GotoDeclarationHandler {

  companion object {
    fun findRefField(attr: XmlAttribute): JSField? {
      val tsClass = getRefTSClass(attr)
      if (tsClass != null) {
        return tsClass.allFields.firstOrNull() {
          it.hasDecorator("Prop") && it.name == attr.name
        }
      }
      return null
    }

    fun findTypeScriptClass(file: PsiFile): TypeScriptClassExpression? {
      if (file is XmlFile) {
        return file.document?.getChildrenOfType<HtmlTag>()
            ?.firstOrNull { it.name == VueArrangementVisitor.SCRIPT_TAG }
            ?.getChildByType<JSEmbeddedContent>()?.getChildOfType<ES6ExportDefaultAssignment>()
            ?.getChildOfType()
      }
      return null
    }


    fun getRefTSClass(element: XmlAttribute): TypeScriptClassExpression? {
      return element.parent?.reference?.resolve()
          ?.ancestorOfType<ES6ExportDefaultAssignment>()
          ?.getChildByType<TypeScriptClassExpression>()
    }
  }

  override fun getActionText(context: DataContext): String? {
    return "none"
  }

  override fun getGotoDeclarationTargets(el: PsiElement?, p1: Int, p2: Editor?): Array<PsiElement>? {
    if (vueEnabled && el != null) {
      if (el.containingFile.language is VueLanguage || el.containingFile.language is VueJSLanguage) {
        if (el.language is XMLLanguage) {
          val field = when {
            el.parent is XmlAttribute -> findRefField(el.parent as XmlAttribute)
            el is XmlAttribute        -> findRefField(el)
            else                      -> null
          }
          if (field != null) {
            return arrayOf(field)
          }
        } else if (el is LeafPsiElement && el.parent is TypeScriptFunction) {
        }
      }
    }
    return arrayOf()
  }

}
