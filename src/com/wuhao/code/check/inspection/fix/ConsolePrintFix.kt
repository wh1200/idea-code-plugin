/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiImportList
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiReferenceExpression
import com.wuhao.code.check.getNewLine
import com.wuhao.code.check.getPsiElementFactory
import com.wuhao.code.check.insertElementAfter
import org.jetbrains.uast.getContainingClass

/**
 * 修复控制台使用非日志输出的方式
 * @author
 * @since
 */
class ConsolePrintFix : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.endElement
    val factory = getPsiElementFactory(el)
    if (el.firstChild is PsiReferenceExpression) {
      val clazz = el.getContainingClass()
      if (clazz != null) {
        val file = clazz.containingFile as PsiJavaFile
        val logField = clazz.allFields.firstOrNull { it.name == LOG_FIELD_NAME }
        if (logField == null) {
          val importList = file.importList
          if (importList != null) {
            val imports = importList.allImportStatements.toList()
            val hasLogImport = imports.any { it.importReference?.text == logPreference }
            val hasLogFactoryImport = imports.any { it.importReference?.text == logFactoryPreference }
            val newImportList = createImportList(project)
            if (!hasLogImport) {
              importList.add(newImportList.importStatements[0])
            }
            if (!hasLogFactoryImport) {
              importList.add(newImportList.importStatements[1])
            }
            val fieldString = "private static final Logger $LOG_FIELD_NAME " +
                "= LoggerFactory.getLogger(${clazz.nameIdentifier?.text}.class)"
            val field = factory.createFieldFromText(fieldString, clazz)
            clazz.lBrace!!.apply {
              insertElementAfter(field)
              insertElementAfter(getNewLine(project))
            }
          }
        }
      }
      if (el.firstChild.text.startsWith(printDeclaration)) {
        el.firstChild.replace(factory.createExpressionFromText("$LOG_FIELD_NAME.info", null))
      } else {
        if (el.firstChild.text.startsWith(errorDeclaration)) {
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
        """import $logPreference;
                  |import $logFactoryPreference;
                """.trimMargin()
    ) as PsiJavaFile).importList!!
  }

  companion object {

    private const val LOG_FIELD_NAME = "LOG"
    private const val errorDeclaration = "System.err.print"
    private const val logFactoryPreference = "org.slf4j.LoggerFactory"
    private const val logPreference = "org.slf4j.Logger"
    private const val printDeclaration = "System.out.print"

  }

}

