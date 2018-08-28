package com.wuhao.code.check

import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.RESULT_MAP_TAG
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.SQL_TAG
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

/**
 * 获取标签的id属性
 */
val XmlTag.id: String?
  get() {
    return this.getAttributeValue(MybatisMapperFileLineMarkerProvider.ID_ATTR)
  }

/**
 * 根据id查找resultMap
 * @param id
 * @return
 */
fun XmlElement.getResultMap(id: String?): XmlTag? {
  return findTagById(id, RESULT_MAP_TAG)
}

/**
 *
 * @param id
 * @return
 */
fun XmlElement.getSQL(id: String?): XmlTag? {
  return findTagById(id, SQL_TAG)
}

/**
 *
 * @param id
 * @param tag
 * @return
 */
private fun XmlElement.findTagById(id: String?, tag: String): XmlTag? {
  val file = this.containingFile as XmlFile
  return file.rootTag?.getChildrenOfType<XmlTag>()
      ?.firstOrNull { it.name == tag && it.id == id }
}

