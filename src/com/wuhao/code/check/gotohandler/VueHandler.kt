package com.wuhao.code.check.gotohandler

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClassExpression
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil.SCRIPT_TAG_NAME
import com.wuhao.code.check.*
import com.wuhao.code.check.style.arrangement.VueRootTagOrderToken
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.VueLanguage

/**
 * Created by 吴昊 on 2017/7/18.
 * @author 吴昊
 * @since 1.3.8
 */
class VueHandler : GotoDeclarationHandler {

  companion object {
    fun findRefField(attr: XmlAttribute): PsiElement? {
      val tsClass = getRefTSClass(attr)
      val matchName = when {
        attr.name.startsWith(":")       -> attr.name.substring(1)
        attr.name.startsWith("v-bind:") -> attr.name.substring(7)
        else                            -> attr.name
      }
      if (tsClass != null) {
        return tsClass.allFields.firstOrNull {
          it.hasDecorator("Prop") && it.name == matchName
        }
      } else {
        val jsObject = getRefObject(attr)
        if (jsObject != null) {
          val propsProperty = jsObject.findProperty("props")
          if (propsProperty != null && propsProperty.value is JSObjectLiteralExpression) {
            val propsValue = propsProperty.value as JSObjectLiteralExpression
            return propsValue.findProperty(matchName)
          }
        }
      }
      return null
    }

    fun findTSClass(file: PsiFile): TypeScriptClassExpression? {
      if (file is XmlFile) {
        return file.document?.getChildrenOfType<HtmlTag>()
            ?.firstOrNull { it.name == SCRIPT_TAG_NAME }
            ?.getChildByType<JSEmbeddedContent>()?.getChildOfType<ES6ExportDefaultAssignment>()
            ?.getChildOfType()
      }
      return null
    }

    fun getRefObject(attr: XmlAttribute): JSObjectLiteralExpression? {
      val tag = attr.parent
      val refEl = tag.reference?.resolve()?.parent
      if (refEl is JSObjectLiteralExpression) {
        return refEl
      }
      return null
    }

    fun getRefTSClass(element: XmlTag): TypeScriptClassExpression? {
      val resolvedRef = element.reference?.resolve()
      if (resolvedRef != null) {
        return if (resolvedRef.parent is JSCallExpression) {
          val callExp = resolvedRef.parent as JSCallExpression
          resolveTSClassFromComponentRegisterCall(callExp)
        } else {
          resolvedRef.ancestorOfType<ES6ExportDefaultAssignment>()
              ?.getChildByType<TypeScriptClassExpression>()
        }
      }
      return null
    }

    fun getRefTSClass(element: XmlAttribute): TypeScriptClassExpression? {
      val tag = element.parent
      return getRefTSClass(tag)
    }

    fun resolveCommonRefFromComponentRegisterCall(callExp: JSCallExpression): PsiElement? {
      val ref = resolveImportRef(callExp)
      if (ref != null) {
        return if (ref is JSFile) {
          ref.getChildOfType<ES6ExportDefaultAssignment>()
        } else {
          ref.getChildOfType<ES6ExportDefaultAssignment>()?.getChildByType<TypeScriptClassExpression>()
        }
      }
      return null
    }

    fun resolveReferenceOfVueComponentTag(element: XmlTag): PsiElement? {
      var res: PsiElement? = null
      if (element.name !in VueRootTagOrderToken.rootTags) {
//        val data = VueComponentsCalculation.calculateScopeComponents(GlobalSearchScope.allScope(project),true)
//        val components = VueComponentsCache.getAllComponentsGroupedByModules((element.project), { true }, true)
//        components.forEach {
//          val moduleName = it.key
//          val toAdd = arrayListOf<Pair<String, Pair<PsiElement, Boolean>>>()
//          it.value.forEach { t, u ->
//            val componentName = if (moduleName == "ant-design-vue" && !t.startsWith("a-")) {
//              if (it.value is LinkedHashMap) {
//                toAdd.add("a-$t" to u)
//              }
//              "a-$t"
//            } else {
//              t
//            }
//            if (element.name == componentName) {
//              val refEl = u.first
//              if (refEl is JSCallExpression && refEl.arguments.size == 2) {
//                val realRef = VueHandler.resolveCommonRefFromComponentRegisterCall(refEl)
//                if (realRef != null) {
//                  res = realRef
//                  return@forEach
//                }
//              }
//            }
//          }
//          val tmp = it.value as LinkedHashMap
//          toAdd.forEach {
//            tmp[it.first] = it.second
//          }
//        }
      }
      return res
    }

    private fun resolveImportRef(callExp: JSCallExpression): PsiElement? {
      if (callExp.arguments.size == 2) {
        val comp = callExp.arguments[1].reference?.resolve()
        if (comp is ES6ImportedBinding) {
          val importReferences = comp.declaration?.fromClause?.resolveReferencedElements()
          if (importReferences != null && importReferences.isNotEmpty()) {
            return importReferences.toTypedArray()[0]
          }
        }
      }
      return null
    }

    private fun resolveTSClassFromComponentRegisterCall(callExp: JSCallExpression): TypeScriptClassExpression? {
      val ref = resolveImportRef(callExp)
      if (ref != null) {
        return ref.getChildOfType<ES6ExportDefaultAssignment>()
            ?.getChildByType<TypeScriptClassExpression>()
      }
      return null
    }
  }

  override fun getActionText(context: DataContext): String? {
    return "none"
  }

  override fun getGotoDeclarationTargets(el: PsiElement?, p1: Int, p2: Editor?): Array<PsiElement>? {
    if (vueEnabled && el != null && (el.containingFile.language is VueLanguage || el.containingFile.language is VueJSLanguage) && el.language is XMLLanguage) {
      val field = when {
        el.parent is XmlAttribute -> findRefField(el.parent as XmlAttribute)
        el is XmlAttribute        -> findRefField(el)
        el.parent is HtmlTag      -> {
          resolveReferenceOfVueComponentTag(el.parent as HtmlTag)
        }
        else                      -> null
      }
      if (field != null) {
        return arrayOf(field)
      }
    }
    return arrayOf()
  }

}
