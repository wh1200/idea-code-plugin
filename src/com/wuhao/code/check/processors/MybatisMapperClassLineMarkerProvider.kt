/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.processors

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.MAPPER_RELATIVE_PATH
import com.wuhao.code.check.processors.MybatisMapperFileLineMarkerProvider.Companion.ID_ATTR_NAME
import org.jetbrains.kotlin.idea.refactoring.toPsiFile
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import java.io.File


/**
 * 为Mybatis的Mapper接口类提供跳转至对应的xml配置文件的gutter
 * @author 吴昊
 * @since 1.0
 */
class MybatisMapperClassLineMarkerProvider : RelatedItemLineMarkerProvider() {

  companion object {
    val ICON_FILE = IconLoader.getIcon("/icons/arrow_down.png")
    const val MAPPER_ANNOTATION_TEXT = "@Mapper"
    const val MAPPER_CLASS = "org.apache.ibatis.annotations.Mapper"
  }

  override fun collectNavigationMarkers(element: PsiElement,
                                        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
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
          val tag = findTag(psiFile as XmlFile, mapperInfo)
          val builder = NavigationGutterIconBuilder.create(ICON_FILE).setTargets(listOf(tag))
              .setTooltipText("跳转至Mapper文件")
          result.add(builder.createLineMarkerInfo(element))
        }
      }
    }
  }

  private fun findTag(xmlFile: XmlFile, mapperInfo: MybatisMapperClassLineMarkerProvider.MapperInfo): XmlTag? {
    val mapperTag = xmlFile.document!!.getChildOfType<XmlTag>()
    return if (mapperInfo.isMethod) {
      mapperTag?.children?.firstOrNull {
        it is XmlTag && it.getAttributeValue(ID_ATTR_NAME) == mapperInfo.methodName
      } as XmlTag?
    } else {
      mapperTag
    }
  }

  private fun isMapperInterface(clazz: PsiClass?): Boolean {
    return clazz != null && clazz.isInterface
        && clazz.annotations.any { it.qualifiedName == MAPPER_CLASS }
  }

  private fun isMapperInterface(clazz: KtClass?): Boolean {
    return clazz != null && clazz.isInterface()
        && clazz.annotationEntries.any { it.text == MAPPER_ANNOTATION_TEXT }
  }

  private fun resolveMapperInfo(el: PsiElement): MapperInfo? {
    if (el is PsiIdentifier
        || (el is LeafPsiElement
            && el.elementType == KtTokens.IDENTIFIER)) {
      val element = el.parent
      when (element) {
        is KtFunction -> {
          val clazz = element.containingClass()
          if (isMapperInterface(clazz)) {
            return MapperInfo(true, clazz!!.name ?: "", element.name)
          }
        }
        is KtClass -> {
          if (isMapperInterface(element)) {
            return MapperInfo(false, element.name ?: "")
          }
        }
        is PsiMethod -> {
          val clazz = element.containingClass
          if (isMapperInterface(clazz)) {
            return MapperInfo(true, clazz!!.name ?: "", element.name)
          }
        }
        is PsiClass -> {
          if (isMapperInterface(element)) {
            return MapperInfo(false, element.name ?: "", null)
          }
        }
      }
    }
    return null
  }

  /**
   * mybatis的Mapper接口中的psi元素与xml配置文件的对应关系
   * @author 吴昊
   * @since 1.1
   * @param isMethod 该元素是否对应一个方法
   * @param mapperName 对应的mapper名称
   * @param methodName 对应的方法名称
   */
  private data class MapperInfo(val isMethod: Boolean, val mapperName: String, val methodName: String? = null)

}

