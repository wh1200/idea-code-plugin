package com.wuhao.code.check.gotohandler

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.xml.*
import com.wuhao.code.check.findPsiFile
import com.wuhao.code.check.getResultMap
import com.wuhao.code.check.getSQL
import com.wuhao.code.check.id
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.EXTENDS_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.ID_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.INCLUDE_TAG
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.REF_ID_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.RESULT_MAP_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.RESULT_MAP_TAG
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.SQL_TAG
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.TYPE_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.isMethodTag
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.resolveMapperClassOrMethod
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

/**
 * Created by 吴昊 on 2017/7/18.
 * @author 吴昊
 * @since 1.3.8
 */
class MyBatisGotoDeclarationHandler : GotoDeclarationHandler {

  override fun getActionText(p0: DataContext?): String? {
    return null
  }

  override fun getGotoDeclarationTargets(el: PsiElement?, p1: Int, p2: Editor?): Array<PsiElement>? {
    if (el != null) {
      if (el.language is KotlinLanguage || el.language is JavaLanguage) {
//        val tag = MybatisMapperClassLineMarkerProvider.resolveMapperXmlTag(el)
//        if (tag != null) {
//          return arrayOf(tag)
//        }
      } else {
        if (el.language is XMLLanguage && el is XmlToken && el.parent is XmlAttributeValue) {
          val value = el.parent as XmlAttributeValue
          val attribute = value.parent as XmlAttribute
          val tag = attribute.parent
          val file = value.containingFile as XmlFile
          when (attribute.name) {
            TYPE_ATTR       -> {
              if (tag.name == RESULT_MAP_TAG) {
                val psiFile = el.project.findPsiFile(attribute.value)
                if (psiFile != null) {
                  return arrayOf(psiFile)
                }
              }
            }
            ID_ATTR         -> {
              if (isMethodTag(tag)) {
                val mapperClassOrMethod = resolveMapperClassOrMethod(tag)
                if (mapperClassOrMethod != null) {
                  return arrayOf(mapperClassOrMethod)
                }
              } else {
                when (tag.name) {
                  SQL_TAG        -> {
                    return findIncludes(value).toTypedArray()
                  }
                  RESULT_MAP_TAG -> {
                    return file.rootTag!!.getChildrenOfType<XmlTag>().filter {
                      it.getAttributeValue(RESULT_MAP_ATTR) == attribute.value
                          || it.getAttributeValue(EXTENDS_ATTR) == attribute.value
                    }.toTypedArray()
                  }
                }
              }
            }
            REF_ID_ATTR     -> {
              val sql = el.getSQL(attribute.value)
              if (sql != null) {
                return arrayOf(sql)
              }
            }
            EXTENDS_ATTR    -> {
              val resultMapTag = el.getResultMap(attribute.value)
              if (resultMapTag != null) {
                return arrayOf(resultMapTag)
              }
            }
            RESULT_MAP_ATTR -> {
              val mapId = attribute.value
              val resultMapTag = el.getResultMap(mapId)
              if (resultMapTag != null) {
                return arrayOf(resultMapTag)
              }
            }
          }
        }
      }
    }
    return arrayOf()
  }

  private fun findIncludes(value: XmlAttributeValue): List<PsiElement> {
    val file = value.containingFile as XmlFile
    val attr = value.parent as XmlAttribute
    val tag = attr.parent as XmlTag
    val id = tag.id
    val result = arrayListOf<PsiElement>()
    if (id != null && tag.name == SQL_TAG) {
      file.accept(object : XmlRecursiveElementVisitor() {

        override fun visitXmlTag(tag: XmlTag) {
          if (tag.name == INCLUDE_TAG && tag.getAttribute(REF_ID_ATTR)?.value == id) {
            result.add(tag)
          }
          this.visitElement(tag)
        }

      })

    }
    return result
  }

}

