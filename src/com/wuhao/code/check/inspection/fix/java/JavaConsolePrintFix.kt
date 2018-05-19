/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix.java

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiImportList
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiReferenceExpression
import com.wuhao.code.check.getNewLine
import com.wuhao.code.check.insertElementAfter
import com.wuhao.code.check.psiElementFactory
import org.jetbrains.uast.getContainingClass

/**
 * 修复控制台使用非日志输出的方式
 * @author
 * @since
 */
class JavaConsolePrintFix : LocalQuickFix {

  companion object {

    const val ERROR_DECLARATION = "System.err.print"
    const val LOG_FACTORY_PREFERENCE = "org.slf4j.LoggerFactory"
    const val LOG_FIELD_NAME = "LOG"
    const val LOG_PREFERENCE = "org.slf4j.Logger"
    private const val PRINT_DECLARATION = "System.out.print"
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.endElement
    val factory = el.psiElementFactory
    if (el.firstChild is PsiReferenceExpression) {
      val clazz = el.getContainingClass()
      if (clazz != null) {
        val file = clazz.containingFile as PsiJavaFile
        val logField = clazz.allFields.firstOrNull { it.name == LOG_FIELD_NAME }
        if (logField == null) {
          val importList = file.importList
          if (importList != null) {
            val imports = importList.allImportStatements.toList()
            val hasLogImport = imports.any { it.importReference?.text == LOG_PREFERENCE }
            val hasLogFactoryImport = imports.any { it.importReference?.text == LOG_FACTORY_PREFERENCE }
            val newImportList = createImportList(project)
            if (!hasLogImport) {
              importList.add(newImportList.importStatements[0])
            }
            if (!hasLogFactoryImport) {
              importList.add(newImportList.importStatements[1])
            }
            val fieldString = "private static final Logger $LOG_FIELD_NAME " +
                "= LoggerFactory.getLogger(${clazz.nameIdentifier?.text}.class);"
            val field = factory.createFieldFromText(fieldString, clazz)
            clazz.lBrace!!.apply {
              insertElementAfter(field)
              insertElementAfter(project.getNewLine())
            }
          }
        }
      }
      if (el.firstChild.text.startsWith(PRINT_DECLARATION)) {
        el.firstChild.replace(factory.createExpressionFromText("$LOG_FIELD_NAME.info", null))
      } else {
        if (el.firstChild.text.startsWith(ERROR_DECLARATION)) {
          el.firstChild.replace(factory.createExpressionFromText("$LOG_FIELD_NAME.error", null))
        }
      }
    }
  }

  override fun getFamilyName(): String {
    return "替换为日志输出"
  }

  private fun createImportList(project: Project): PsiImportList {
    return (PsiFileFactory.getInstance(project).createFileFromText(
        "Dummy", JavaFileType.INSTANCE,
        """import $LOG_PREFERENCE;
                  |import $LOG_FACTORY_PREFERENCE;
                """.trimMargin()
    ) as PsiJavaFile).importList!!
  }

}

