package com.wuhao.code.check.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.wuhao.code.check.constants.InspectionNames.MYBATIS
import com.wuhao.code.check.constants.registerWarning
import com.wuhao.code.check.getResultMap
import com.wuhao.code.check.getSQL
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.EXTENDS_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.ID_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.INCLUDE_TAG
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.REF_ID_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.RESULT_MAP_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.isMethodTag
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

/**
 *
 * Created by 吴昊 on 2018/6/28.
 *
 * @author 吴昊
 * @since
 */
class MybatisMapperInspection : BaseInspection(MYBATIS) {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : XmlElementVisitor() {

      override fun visitXmlAttribute(attribute: XmlAttribute) {
        val tag = attribute.parent
        val valueElement = attribute.valueElement
        if (valueElement != null) {
          val nameToken = valueElement.getChildrenOfType<XmlToken>().firstOrNull { it.text != "\"" }
          if (nameToken != null) {
            when (attribute.name) {
              RESULT_MAP_ATTR -> {
                if (isMethodTag(tag)) {
                  val resultMap = tag.getResultMap(attribute.value)
                  if (resultMap == null) {
                    holder.registerWarning(nameToken, "resultMap不存在")
                  }
                }
              }
              EXTENDS_ATTR -> {
                val resultMap = tag.getResultMap(attribute.value)
                if (resultMap == null) {
                  holder.registerWarning(nameToken, "resultMap不存在")
                }
              }
              REF_ID_ATTR -> {
                if (tag.name == INCLUDE_TAG) {
                  val sql = tag.getSQL(attribute.value)
                  if (sql == null) {
                    holder.registerWarning(nameToken, "sql模板不存在")
                  }
                }
              }
            }
          }
        }
      }

      override fun visitXmlFile(file: XmlFile) {
        val tags = file.rootTag?.getChildrenOfType<XmlTag>()
        if (tags != null) {
          val map = tags.groupBy { it.getAttributeValue(ID_ATTR) }
          map.values.forEach { list ->
            if (list.size > 1) {
              list.drop(1).forEach {
                val idValueElement = it.getAttribute(ID_ATTR)?.valueElement
                val nameToken = idValueElement?.getChildrenOfType<XmlToken>()?.firstOrNull { it.text != "\"" }
                if (idValueElement != null && nameToken != null) {
                  holder.registerWarning(nameToken, "重复的ID")
                }
              }
            }
          }
        }
      }

      override fun visitXmlTag(tag: XmlTag) {
      }

    }
  }

}

