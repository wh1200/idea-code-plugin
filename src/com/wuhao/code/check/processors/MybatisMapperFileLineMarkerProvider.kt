/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.processors

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
    val ICON_FILE = IconLoader.getIcon("/icons/arrow_up.png")
    const val ID_ATTR_NAME = "id"
    const val INSERT = "insert"
    const val MAPPER_NAMESPACE_ATTR_NAME = "namespace"
    const val MAPPER_TAG_NAME = "mapper"
    const val SELECT = "select"
    const val UPDATE = "update"
  }

  override fun collectNavigationMarkers(element: PsiElement,
                                        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
    val mapperInfo = resolveMapperInfo(element)
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
                  result.add(createLineMarkerInfo(element, clazz.nameIdentifier!!))
                } else {
                  val method = clazz.methods.firstOrNull { it.name == mapperInfo.methodName }
                  if (method != null) {
                    result.add(createLineMarkerInfo(element, method.nameIdentifier!!))
                  }
                }
              }
            } else if (psiFile is KtFile && psiFile.classes.size == 1) {
              val clazz = psiFile.classes[0] as KtLightClass
              if (clazz.isInterface) {
                if (!mapperInfo.isMethod) {
                  result.add(createLineMarkerInfo(element, clazz.nameIdentifier!!))
                } else {
                  val function = clazz.methods
                      .firstOrNull { it.name == mapperInfo.methodName }
                  if (function != null) {
                    result.add(createLineMarkerInfo(element, function.nameIdentifier!!))
                  }
                }
              }
            }
          }
          return
        }
      }
    }
  }

  private fun createLineMarkerInfo(source: PsiElement, target: PsiElement): RelatedItemLineMarkerInfo<*> {
    val builder = NavigationGutterIconBuilder.create(ICON_FILE).setTargets(listOf(target))
        .setTooltipText(Messages.JUMP_TO_INTERFACE)
    return builder.createLineMarkerInfo(source)
  }

  private fun findSourceFile(
      root: VirtualFile,
      mapperInfo: MybatisMapperFileLineMarkerProvider.MapperInfo)
      : VirtualFile? {
    val javaFile = root.findFileByRelativePath(File.separator + mapperInfo.getJavaClasspath())
    return javaFile ?: root.findFileByRelativePath(File.separator
        + mapperInfo.getKotlinClasspath())
  }

  private fun isMapperTag(el: PsiElement): Boolean {
    return el is XmlTag && el.name == MAPPER_TAG_NAME
  }

  private fun isMethodTag(el: PsiElement): Boolean {
    return el is XmlTag && isMapperTag(el.parent)
        && el.name in listOf(UPDATE, INSERT, DELETE, SELECT)
  }

  private fun resolveMapperInfo(el: PsiElement): MapperInfo? {
    if (el is XmlTag) {
      if (isMapperTag(el)) {
        val classpath = el.getAttributeValue(MAPPER_NAMESPACE_ATTR_NAME)
        if (classpath != null) {
          return MapperInfo(false, classpath)
        }
      } else if (isMethodTag(el)) {
        val classpath = (el.parent as XmlTag).getAttributeValue(MAPPER_NAMESPACE_ATTR_NAME)
        val methodName = el.getAttributeValue(ID_ATTR_NAME)
        if (classpath != null) {
          return MapperInfo(true, classpath, methodName)
        }
      }
    }
    return null
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

