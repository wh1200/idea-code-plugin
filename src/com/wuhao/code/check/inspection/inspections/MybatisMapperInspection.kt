package com.wuhao.code.check.inspection.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlRecursiveElementVisitor
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlDoctype
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.wuhao.code.check.constants.InspectionNames.MYBATIS
import com.wuhao.code.check.constants.registerError
import com.wuhao.code.check.getResultMap
import com.wuhao.code.check.getSQL
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.DELETE_TAG
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.INCLUDE_TAG
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.INSERT_TAG
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.REF_ID_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.RESULT_MAP_ATTR
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.SELECT_TAG
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.UPDATE_TAG
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
    return object : XmlRecursiveElementVisitor() {

      override fun visitXmlAttribute(attribute: XmlAttribute) {
        val tag = attribute.parent
        val nameToken = attribute.valueElement!!.getChildrenOfType<XmlToken>().first { it.text != "\"" }
        if (tag.name in listOf(DELETE_TAG, UPDATE_TAG, INSERT_TAG, SELECT_TAG)
            && attribute.name == RESULT_MAP_ATTR) {
          val resultMap = tag.getResultMap(attribute.value)
          if (resultMap == null) {
            holder.registerError(nameToken, "resultMap不存在")
          }
        }
        if (tag.name == INCLUDE_TAG && attribute.name == REF_ID_ATTR) {
          val sql = tag.getSQL(attribute.value)
          if (sql == null) {
            holder.registerError(nameToken, "sql模板不存在")
          }
        }
      }

      override fun visitXmlDoctype(xmlDoctype: XmlDoctype) {
      }

      override fun visitXmlTag(tag: XmlTag) {
      }

    }
  }

}

