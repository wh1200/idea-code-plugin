/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.linemarker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.constants.Annotations.IBATIS_MAPPER
import com.wuhao.code.check.constants.MAPPER_RELATIVE_PATH
import com.wuhao.code.check.hasAnnotation
import com.wuhao.code.check.linemarker.MybatisMapperFileLineMarkerProvider.Companion.ID_ATTR
import com.wuhao.code.check.toPsiFile
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import java.io.File

/**
 * 为Mybatis的Mapper接口类提供跳转至对应的xml配置文件的gutter
 * @author 吴昊
 * @since 1.0
 */
class MybatisMapperClassLineMarkerProvider : RelatedItemLineMarkerProvider() {

  companion object {
    val ICON_FILE = IconLoader.getIcon("/icons/arrow_down.png")

    fun resolveMapperXmlTag(element: PsiElement): XmlTag? {
      val mapperInfo = resolveMapperInfo(element)
      if (mapperInfo != null && mapperInfo.mapperName.isNotBlank()) {
        val project = element.project
        val psiManager = PsiManager.getInstance(project)
        val fileSystem = project.projectFile!!.fileSystem
        val file = fileSystem.findFileByPath(project.basePath +
            File.separator + "$MAPPER_RELATIVE_PATH${File.separator}${mapperInfo.mapperName}.xml")
        if (file != null) {
          val psiFile = psiManager.findFile(file) ?: file.toPsiFile(project)
          if (psiFile != null) {
            return findTag(psiFile as XmlFile, mapperInfo)
          }
        }
      }
      return null
    }

    private fun findTag(xmlFile: XmlFile, mapperInfo: MapperInfo): XmlTag? {
      val rootTag = xmlFile.rootTag
      return if (mapperInfo.isMethod) {
        rootTag?.children?.firstOrNull {
          it is XmlTag && it.getAttributeValue(ID_ATTR) == mapperInfo.methodName
        } as XmlTag?
      } else {
        rootTag
      }
    }

    private fun isMapperInterface(clazz: PsiClass?): Boolean {
      return clazz != null && clazz.isInterface
          && clazz.annotations.any { it.qualifiedName == IBATIS_MAPPER }
    }

    private fun isMapperInterface(clazz: KtClass?): Boolean {
      return clazz != null && clazz.isInterface()
          && clazz.hasAnnotation(IBATIS_MAPPER)
    }

    private fun resolveMapperInfo(el: PsiElement): MapperInfo? {
      if (el is PsiIdentifier
          || (el is LeafPsiElement && el.elementType == KtTokens.IDENTIFIER)) {
        val element = el.parent
        when (element) {
          is KtFunction -> {
            val clazz = element.containingClass()
            if (isMapperInterface(clazz)) {
              return MapperInfo(true, clazz!!.name
                  ?: "", element.name)
            }
          }
          is KtClass    -> {
            if (isMapperInterface(element)) {
              return MapperInfo(false, element.name ?: "")
            }
          }
          is PsiMethod  -> {
            val clazz = element.containingClass
            if (isMapperInterface(clazz)) {
              return MapperInfo(true, clazz!!.name ?: "", element.name)
            }
          }
          is PsiClass   -> {
            if (isMapperInterface(element)) {
              return MapperInfo(false, element.name ?: "", null)
            }
          }
        }
      }
      return null
    }
  }

  override fun collectNavigationMarkers(element: PsiElement,
                                        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
    val tag = resolveMapperXmlTag(element)
    if (tag != null) {
      val builder = NavigationGutterIconBuilder.create(ICON_FILE).setTargets(listOf(tag))
          .setTooltipText("跳转至Mapper文件")
      result.add(builder.createLineMarkerInfo(element))
    }
  }

  /**
   * mybatis的Mapper接口中的psi元素与xml配置文件的对应关系
   * @author 吴昊
   * @since 1.1
   * @param isMethod 该元素是否对应一个方法
   * @param mapperName 对应的mapper名称
   * @param methodName 对应的方法名称
   */
  data class MapperInfo(val isMethod: Boolean, val mapperName: String, val methodName: String? = null)

}

