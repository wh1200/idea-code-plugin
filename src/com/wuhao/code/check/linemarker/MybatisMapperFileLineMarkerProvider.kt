/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.linemarker

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.roots.JavaProjectRootsUtil
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.wuhao.code.check.ancestorOfType
import com.wuhao.code.check.constants.Messages
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.idea.refactoring.toPsiFile
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

/**
 * 为Mybatis的mapper配置文件提供跳转至对应的Mapper接口类的gutter
 * @author 吴昊
 * @since 1.1
 */
class MybatisMapperFileLineMarkerProvider : RelatedItemLineMarkerProvider() {

  companion object {
    const val DELETE = "delete"
    const val DELETE_TAG = "delete"
    val ICON_FILE = IconLoader.getIcon("/icons/arrow_up.png")
    const val ID_ATTR = "id"
    const val INCLUDE_TAG = "include"
    const val INSERT_TAG = "insert"
    const val MAPPER_NAMESPACE_ATTR_NAME = "namespace"
    const val MAPPER_TAG = "mapper"
    const val REF_ID_ATTR = "refid"
    const val RESULT_MAP_ATTR = "resultMap"
    const val RESULT_MAP_TAG = "resultMap"
    const val SELECT_TAG = "select"
    const val SQL_TAG = "sql"
    const val UPDATE_TAG = "update"

    /**
     * @param mode 匹配模式，1为lineMarker，2为gotohandler
     */
    fun resolveMapperClassOrMethod(element: PsiElement, mode: Int): PsiElement? {
      val mapperInfo = resolveMapperInfo(element, mode)
      if (mapperInfo != null && mapperInfo.className.isNotBlank()) {
        val project = element.project
        val sourceRoots = JavaProjectRootsUtil.getSuitableDestinationSourceRoots(project)
        val psiManager = PsiManager.getInstance(project)
        sourceRoots.forEach {
          val classFile = findSourceFile(it, mapperInfo)
          if (classFile != null) {
            val psiFile = psiManager.findFile(classFile) ?: classFile.toPsiFile(project)
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
            return null
          }
        }
      }
      return null
    }

    private fun findSourceFile(root: VirtualFile, mapperInfo: MapperInfo)
        : VirtualFile? {
      val javaFile = root.findFileByRelativePath(File.separator + mapperInfo.getJavaClasspath())
      return javaFile ?: root.findFileByRelativePath(File.separator
          + mapperInfo.getKotlinClasspath())
    }

    private fun isMapperTag(el: PsiElement): Boolean {
      return el is XmlTag && el.name == MAPPER_TAG
    }

    private fun isMethodTag(el: PsiElement): Boolean {
      return el is XmlTag && isMapperTag(el.parent)
          && el.name in listOf(UPDATE_TAG, INSERT_TAG, DELETE, SELECT_TAG)
    }

    private fun resolveMapperInfo(el: PsiElement, mode: Int): MapperInfo? {
      var tag: PsiElement? = el
      if (mode == 2 && el is XmlToken) {
        tag = el.ancestorOfType<XmlTag>()
      }
      if (tag is XmlTag) {
        if (isMapperTag(tag)) {
          val classpath = tag.getAttributeValue(MAPPER_NAMESPACE_ATTR_NAME)
          if (classpath != null) {
            return MapperInfo(false, classpath)
          }
        } else if (isMethodTag(tag)) {
          val classpath = (tag.parent as XmlTag).getAttributeValue(MAPPER_NAMESPACE_ATTR_NAME)
          val methodName = tag.getAttributeValue(ID_ATTR)
          if (classpath != null) {
            return MapperInfo(true, classpath, methodName)
          }
        }
      }
      return null
    }
  }

  override fun collectNavigationMarkers(element: PsiElement,
                                        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
    val relatedClassOrMethod = resolveMapperClassOrMethod(element, 1)
    if (relatedClassOrMethod != null) {
      result.add(createLineMarkerInfo(element, relatedClassOrMethod))
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
  private data class MapperInfo(val isMethod: Boolean, val className: String, val methodName: String? = null) {

    fun getJavaClasspath(): String {
      return className.replace(".", File.separator) + ".java"
    }

    fun getKotlinClasspath(): String {
      return className.replace(".", File.separator) + ".kt"
    }

  }

}

