/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix.kotlin

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.wuhao.code.check.getNewLine
import com.wuhao.code.check.insertElementAfter
import com.wuhao.code.check.inspection.fix.java.JavaConsolePrintFix.Companion.ERROR_DECLARATION
import com.wuhao.code.check.inspection.fix.java.JavaConsolePrintFix.Companion.LOG_FACTORY_PREFERENCE
import com.wuhao.code.check.inspection.fix.java.JavaConsolePrintFix.Companion.LOG_FIELD_NAME
import com.wuhao.code.check.inspection.fix.java.JavaConsolePrintFix.Companion.LOG_PREFERENCE
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.core.getOrCreateCompanionObject
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClass

/**
 * 修复控制台使用非日志输出的方式
 * @author
 * @since
 */
class KotlinConsolePrintFix : LocalQuickFix {

  companion object {
    private const val PRINT_DECLARATION = "println"
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.endElement
    val factory = KtPsiFactory(project)
    if (el is KtReferenceExpression) {
      val clazz = el.containingClass()
      if (clazz != null) {
        val file = clazz.containingFile as KtFile
        val companion = clazz.getOrCreateCompanionObject()
        val logField = companion.getBody()?.properties?.firstOrNull { it.name == LOG_FIELD_NAME }
        if (logField == null) {
          val importList = file.importList
          if (importList != null) {
            val imports = importList.imports
            val hasLogImport = imports.any { it.importedReference?.text == LOG_PREFERENCE }
            val hasLogFactoryImport = imports.any { it.importedReference?.text == LOG_FACTORY_PREFERENCE }
            val newImportList = createImportList(project)
            if (!hasLogImport) {
              importList.add(newImportList.imports[0])
            }
            if (!hasLogFactoryImport) {
              importList.add(newImportList.imports[1])
            }
            val fieldString = "private val $LOG_FIELD_NAME: Logger " +
                "= LoggerFactory.getLogger(${clazz.nameIdentifier?.text}::class.java)"
            val field = factory.createProperty(fieldString)
            companion.getBody()?.lBrace?.apply {
              insertElementAfter(field)
              insertElementAfter(getNewLine(project))
            }
          }
        }
      }
      val callExpression = el.parent as KtCallExpression
      if (el.text.startsWith(PRINT_DECLARATION)) {
        callExpression.replace(factory.createExpression("$LOG_FIELD_NAME.info${callExpression.valueArgumentList?.text}"))
      } else if (el.text.startsWith(ERROR_DECLARATION)) {
        callExpression.replace(factory.createExpression("$LOG_FIELD_NAME.error${callExpression.valueArgumentList?.text}"))
      }
    }
  }

  override fun getFamilyName(): String {
    return "替换为日志输出"
  }

  private fun createImportList(project: Project): KtImportList {
    return (PsiFileFactory.getInstance(project).createFileFromText(
        "Dummy", KotlinFileType.INSTANCE, """
          import $LOG_PREFERENCE
          import $LOG_FACTORY_PREFERENCE
          """.trimMargin()
    ) as KtFile).importList!!
  }

}

