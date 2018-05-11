/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.gotohandler

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.spring.boot.SpringBootConfigFileConstants
import com.wuhao.code.check.JAVA_VALUE_ANNOTATION_PATTERN
import com.wuhao.code.check.RESOURCES_PATH
import com.wuhao.code.check.ancestors
import com.wuhao.code.check.lang.RecursiveVisitor
import org.jetbrains.kotlin.idea.refactoring.toPsiFile
import org.jetbrains.yaml.psi.YAMLKeyValue

/**
 * Created by 吴昊 on 2017/7/18.
 * @author 吴昊
 * @since 1.0
 */
class GotoSpringBootConfigPropertyDeclarationHandler : GotoDeclarationHandler {

  override fun getActionText(context: DataContext?): String? {
    return null
  }

  override fun getGotoDeclarationTargets(el: PsiElement?, p1: Int, p2: Editor?): Array<PsiElement>? {
    val res = arrayListOf<PsiElement>()
    if (el != null && JAVA_VALUE_ANNOTATION_PATTERN.accepts(el)) {
      val project = el.project
      val yamlFile = el.containingFile.virtualFile.fileSystem
          .findFileByPath("${project.basePath}/$RESOURCES_PATH/${SpringBootConfigFileConstants.APPLICATION_YML}")?.toPsiFile(project)
      val currentKey = getRealProperty(el.text)
      if (yamlFile != null) {
        object : RecursiveVisitor() {
          override fun visitElement(element: PsiElement) {
            if (element is YAMLKeyValue) {
              val key = element.ancestors.filter { it is YAMLKeyValue }
                  .reversed().joinToString(".") {
                    (it as YAMLKeyValue).keyText
                  }
              if (key == currentKey) {
                res.add(element)
              }
            }
          }
        }.visit(yamlFile)
      }
    }
    return res.toTypedArray()
  }

  private fun getRealProperty(text: String): String {
    val quoteLength = when {
      text.startsWith("\"\"\"") -> 4
      else -> 3
    }
    val tmp = text.substring(quoteLength)
    return tmp.substring(0, tmp.length - quoteLength + 1)
  }
}
