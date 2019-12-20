package com.wuhao.code.check.action

import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile
import com.wuhao.code.check.constants.Annotations.IBATIS_MAPPER
import com.wuhao.code.check.hasAnnotation
import org.jetbrains.kotlin.idea.core.util.toPsiDirectory
import org.jetbrains.kotlin.idea.refactoring.psiElement
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import org.jetbrains.kotlin.idea.util.projectStructure.getModuleDir
import org.jetbrains.kotlin.idea.util.sourceRoots
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile

/**
 *
 * @author 吴昊
 * @since 1.4.7
 */
class CreateMyBatisMapperAction : AnAction() {

  override fun actionPerformed(e: AnActionEvent) {
    val el = e.dataContext.psiElement!!
    createMapperFile(e.project!!, el)
  }

  private fun createMapperFile(project: Project, el: PsiElement) {
    val daoName = getDaoName(el)
    val mapperName = if (daoName != null) {
      when {
        daoName.contains(".") -> daoName.substring(daoName.lastIndexOf(".") + 1) + ".xml"
        else                  -> "$daoName.xml"
      }
    } else {
      "UnknownMapper.xml"
    }
    val currentModule = project.allModules().firstOrNull {
      el.containingFile.virtualFile.path.contains(it.getModuleDir())
    }
    val root = currentModule?.sourceRoots?.firstOrNull {
      it.path.endsWith("src/main/resources")
    }?.toPsiDirectory(project)

    if (root != null) {
      WriteCommandAction.runWriteCommandAction(project) {
        val mapperDir = root.findSubdirectory("mapper") ?: root.createSubdirectory("mapper")
        val newFile = PsiFileFactory.getInstance(project)
            .createFileFromText(mapperName, XMLLanguage.INSTANCE, getMapperContent(daoName))
        mapperDir.add(newFile)
      }
    }
  }

  private fun getDaoName(el: PsiElement): String? {
    if (el is PsiJavaFile && el.classes.isNotEmpty()) {
      val mapperClass = el.classes.firstOrNull {
        it.hasAnnotation(IBATIS_MAPPER)
      }
      if (mapperClass != null) {
        return mapperClass.qualifiedName
      }
    } else if (el is KtFile) {
      val mapperClass = el.classes.firstOrNull {
        it.hasAnnotation(IBATIS_MAPPER)
      }
      if (mapperClass != null) {
        return mapperClass.qualifiedName
      }
    } else if (el is KtClass && el.hasAnnotation(IBATIS_MAPPER)) {
      return el.fqName!!.asString()
    }
    return null
  }

  private fun getMapperContent(daoName: String?): CharSequence {
    return """<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${daoName ?: ""}">
</mapper>""".trimMargin()
  }

}
