package com.wuhao.code.check.gotohandler

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.xml.*
import com.wuhao.code.check.getResultMap
import com.wuhao.code.check.id
import com.wuhao.code.check.linemarker.MybatisMapperClassLineMarkerProvider
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.INCLUDE_TAG
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.REF_ID_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.RESULT_MAP_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.SQL_TAG
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.resolveMapperClassOrMethod
import org.jetbrains.kotlin.idea.KotlinLanguage

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
        val tag = MybatisMapperClassLineMarkerProvider.resolveMapperXmlTag(el)
        if (tag != null) {
          return arrayOf(tag)
        }
      } else {
        if (el.language is XMLLanguage) {
          if (el is XmlTag) {
            val mapId = el.getAttributeValue(RESULT_MAP_ATTR)
            val resultMapTag = el.getResultMap(mapId)
            if (resultMapTag != null) {
              return arrayOf(resultMapTag)
            }
          }
          if (el is XmlToken) {
            val mapperClassOrMethod = resolveMapperClassOrMethod(el,2)
            if (mapperClassOrMethod != null) {
              return arrayOf(mapperClassOrMethod)
            }
            val value = el.parent
            if (value is XmlAttributeValue) {
              val file = value.containingFile
              if (file is XmlFile) {
                val sql = findSql(value)
                return if (sql != null) {
                  arrayOf(sql)
                } else {
                  findIncludes(value).toTypedArray()
                }
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

  private fun findSql(value: PsiElement): PsiElement? {
    val file = value.containingFile as XmlFile
    val mapper = file.rootTag!!
    val attr = value.parent as XmlAttribute
    val tag = attr.parent as XmlTag
    if (attr.name == REF_ID_ATTR && tag.name == INCLUDE_TAG) {
      val refId = attr.value
      val sql = mapper.subTags.firstOrNull {
        it.name == SQL_TAG && it.id == refId
      }
      if (sql != null) {
        return sql
      }
    }
    return null
  }

}

