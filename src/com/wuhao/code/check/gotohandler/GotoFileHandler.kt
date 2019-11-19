/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.gotohandler

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.freemarker.psi.FtlStringLiteral
import com.intellij.freemarker.psi.directives.FtlIncludeDirective
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.actionSystem.DataContextimport com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.wuhao.code.check.constants.RESOURCES_PATH
import com.wuhao.code.check.toPsiFile
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.kotlin.js.translate.utils.finalElement

/**
 * Created by 吴昊 on 2017/7/18.
 * @author 吴昊
 * @since 1.0
 */
class GotoFileHandler : GotoDeclarationHandler {

  fun freemarkerEnabled(): Boolean {
    return PluginManager.getPlugins().any { it.name == "FreeMarker" }
  }

  override fun getActionText(context: DataContext): String? {
    return null
  }

  override fun getGotoDeclarationTargets(el: PsiElement?, p1: Int, p2: Editor?): Array<PsiElement>? {
    if (el != null) {
      if (isMavenProject(el)) {
        val staticPath = "${el.project.basePath}/$RESOURCES_PATH/static/" + el.text
        val pattern = psiElement(XmlToken::class.java)
            .withParent(XmlAttributeValue::class.java)
            .withSuperParent(2, psiElement(XmlAttribute::class.java).withName("src"))
            .withSuperParent(3, psiElement(XmlTag::class.java).withName("script"))
        if (pattern.accepts(el)) {
          return toFilePath(staticPath, el)
        } else if (this.freemarkerEnabled()) {
          val includePathPattern = psiElement(LeafPsiElement::class.java)
              .withParent(FtlStringLiteral::class.java)
              .withSuperParent(2, psiElement(FtlIncludeDirective::class.java))
          val templatePath = "${el.project.basePath}/$RESOURCES_PATH/templates/" + el.text
          if (includePathPattern.accepts(el)) {
            return toFilePath(templatePath, el)
          }
        }
      }
    }
    return arrayOf()
  }

  private fun isMavenProject(el: PsiElement): Boolean {
    val mavenProjectsManager = MavenProjectsManager.getInstance(el.project)
    if (mavenProjectsManager != null && mavenProjectsManager.hasProjects()) {
      return mavenProjectsManager.projects.size > 0
    }
    return false
  }

  private fun toFilePath(staticPath: String, el: PsiElement): Array<PsiElement> {
    val file = LocalFileSystem.getInstance().findFileByPath(staticPath)
    if (file != null && file.exists()) {
      val psiFile = file.toPsiFile(el.project)
      if (psiFile != null) {
        return arrayOf(psiFile.finalElement)
      }
    }
    return arrayOf()
  }

}
