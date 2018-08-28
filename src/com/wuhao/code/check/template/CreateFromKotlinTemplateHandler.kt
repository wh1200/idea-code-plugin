package com.wuhao.code.check.template

import com.intellij.ide.IdeBundle
import com.intellij.ide.fileTemplates.CreateFromTemplateHandler
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.IncorrectOperationException
import com.wuhao.code.check.ui.PluginSettings
import org.jetbrains.kotlin.idea.KotlinFileType

/**
 * @author 吴昊
 * @since
 */
class CreateFromKotlinTemplateHandler : CreateFromTemplateHandler {

  override fun canCreate(dirs: Array<PsiDirectory>): Boolean {
    return true
  }

  @Throws(IncorrectOperationException::class)
  override fun createFromTemplate(project: Project, directory: PsiDirectory, fileName: String, template: FileTemplate, templateText: String, props: Map<String, Any>): PsiElement {
    var copyFileName = fileName
    copyFileName = this.checkAppendExtension(copyFileName, template)
    if (FileTypeManager.getInstance().isFileIgnored(copyFileName)) {
      throw IncorrectOperationException("This filename is ignored (Settings | Editor | File Types | Ignore files and folders)")
    } else {
      directory.checkCreateFile(copyFileName)
      val type = FileTypeRegistry.getInstance().getFileTypeByFileName(copyFileName)
      var file: PsiFile? = PsiFileFactory.getInstance(project).createFileFromText(copyFileName, type, templateText)
      if (template.isReformatCode) {
        CodeStyleManager.getInstance(project).reformat(file!!)
      }
      file = directory.add(file!!) as PsiFile
      return file
    }
  }

  override fun getErrorMessage(): String {
    return IdeBundle.message("title.cannot.create.file", *arrayOfNulls(0))
  }

  override fun handlesTemplate(template: FileTemplate): Boolean {
    return template.isTemplateOfType(KotlinFileType.INSTANCE as FileType)
  }

  override fun isNameRequired(): Boolean {
    return true
  }

  override fun prepareProperties(props: MutableMap<String, Any>) {
    props["USER"] = PluginSettings.INSTANCE.user
  }

  private fun checkAppendExtension(fileName: String, template: FileTemplate): String {
    var copyFileName = fileName
    val suggestedFileNameEnd = "." + template.extension
    if (!copyFileName.endsWith(suggestedFileNameEnd)) {
      copyFileName += suggestedFileNameEnd
    }
    return copyFileName
  }

}

