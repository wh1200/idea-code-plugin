/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.linemarker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.findPsiFile
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.psi.KtFile

/**
 * 为Mybatis的mapper配置文件提供跳转至对应的Mapper接口类的gutter
 * @author 吴昊
 * @since 1.1
 */
class MybatisMapperFileLineMarkerProvider : RelatedItemLineMarkerProvider() {

  companion object {
    const val EXTENDS_ATTR = "extends"
    val ICON_FILE = IconLoader.getIcon("/icons/arrow_up.png")
    const val ID_ATTR = "id"
    const val TYPE_ATTR = "type"
    const val INCLUDE_TAG = "include"
    const val REF_ID_ATTR = "refid"
    const val RESULT_MAP_ATTR = "resultMap"
    const val RESULT_MAP_TAG = "resultMap"
    const val SQL_TAG = "sql"
    private const val DELETE_TAG = "delete"
    private const val INSERT_TAG = "insert"
    private const val MAPPER_TAG = "mapper"
    private const val NAMESPACE_ATTR_NAME = "namespace"
    private const val SELECT_TAG = "select"
    private const val UPDATE_TAG = "update"

    fun isMethodTag(el: PsiElement): Boolean {
      return el is XmlTag && isMapperTag(el.parent) && el.name in listOf(UPDATE_TAG, INSERT_TAG, DELETE_TAG, SELECT_TAG)
    }

    fun resolveMapperClassOrMethod(element: XmlTag): PsiElement? {
      val mapperInfo = resolveMapperInfo(element)
      if (mapperInfo != null && mapperInfo.className.isNotBlank()) {
        val project = element.project
        val psiFile = project.findPsiFile(mapperInfo.className)
        if (psiFile != null) {
          if (psiFile is PsiJavaFile && psiFile.classes.size == 1) {
            val clazz = psiFile.classes[0]
            if (clazz.isInterface) {
              if (!mapperInfo.isMethod) {
                return clazz.nameIdentifier
              } else {
                val method = clazz.methods.firstOrNull { it.name == mapperInfo.methodName }
                if (method != null) {
                  return method.nameIdentifier
                }
              }
            }
          } else if (psiFile is KtFile && psiFile.classes.size == 1) {
            val clazz = psiFile.classes[0] as KtLightClass
            if (clazz.isInterface) {
              if (!mapperInfo.isMethod) {
                return clazz.nameIdentifier
              } else {
                val function = clazz.methods.firstOrNull { it.name == mapperInfo.methodName }
                if (function != null) {
                  return function.nameIdentifier
                }
              }
            }
          }
        }
      }
      return null
    }

    private fun isMapperTag(el: PsiElement): Boolean {
      return el is XmlTag && el.name == MAPPER_TAG
    }

    private fun resolveMapperInfo(tag: XmlTag): MapperInfo? {
      if (isMapperTag(tag)) {
        val classpath = tag.getAttributeValue(NAMESPACE_ATTR_NAME)
        if (classpath != null) {
          return MapperInfo(false, classpath)
        }
      } else if (isMethodTag(tag)) {
        val classpath = (tag.parent as XmlTag).getAttributeValue(NAMESPACE_ATTR_NAME)
        val methodName = tag.getAttributeValue(ID_ATTR)
        if (classpath != null) {
          return MapperInfo(true, classpath, methodName)
        }
      }
      return null
    }
  }

  override fun collectNavigationMarkers(element: PsiElement,
                                        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
    if (element is XmlTag) {
      val relatedClassOrMethod = resolveMapperClassOrMethod(element)
      if (relatedClassOrMethod != null) {
        result.add(createLineMarkerInfo(element, relatedClassOrMethod))
      }
    }
  }

  private fun createLineMarkerInfo(source: PsiElement, target: PsiElement): RelatedItemLineMarkerInfo<*> {
    val builder = NavigationGutterIconBuilder.create(ICON_FILE).setTargets(listOf(target))
        .setTooltipText(Messages.JUMP_TO_INTERFACE)
    return builder.createLineMarkerInfo(source)
  }

  /**
   * @author 吴昊
   * @since 1.1
   * @param isMethod 该元素是否对应一个方法
   * @param className 对应的Mapper接口类
   * @param methodName 对应的方法名称
   */
  private data class MapperInfo(val isMethod: Boolean,
                                val className: String,
                                val methodName: String? = null)

}

