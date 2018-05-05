/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.wuhao.code.check.ancestors
import com.wuhao.code.check.getAncestor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.endOffset

/**
 * Created by 吴昊 on 2017/7/17.
 */
class CodeCompletion : CompletionContributor() {

  init {
    val pattern = PlatformPatterns.psiElement()
    val completionProvider = JSRequireCompletionProvider()
    extend(CompletionType.BASIC, pattern, completionProvider)
  }

  inner class JSRequireCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(completionParameters: CompletionParameters,
                                processingContext: ProcessingContext,
                                completionResultSet: CompletionResultSet) {
      val psiElement = completionParameters.position
      if (psiElement is PsiComment) {
        return
      }
      if (psiElement.parent is JsonStringLiteral && psiElement.getAncestor(3) is JsonFile) {
        addBeanNameLookupItems(psiElement, completionResultSet)
      } else {
        addPropertyLookupItems(psiElement, completionResultSet)
      }
    }

    private fun addPropertyLookupItems(psiElement: PsiElement, completionResultSet: CompletionResultSet) {
      val project = psiElement.project
      val psiFiles = getAllFiles(project)
      val jsonArray = psiElement.parent.parent
      if (jsonArray is JsonArray) {
        val arrayParent = jsonArray.parent
        val beanPropertyItem = arrayParent.parent.parent
        var className = ""
        if (beanPropertyItem is JsonProperty) {
          className = beanPropertyItem.name
        }
        val psiFile = psiFiles.firstOrNull {
          it.name.replaceFirst(".kt", "").replaceFirst(".java", "") == className
        }
        if (psiFile != null) {
          if (arrayParent is JsonProperty && (arrayParent.name == "includes" || arrayParent.name == "excludes")) {
            val existsValues = jsonArray.valueList.map {
              it.text.replace("\"", "")
            }
            val fields = getFieldNames(psiFile, existsValues)
            fields.forEach {
              val builder = LookupElementBuilder
                .create(it.name)
                .withBoldness(true)
                .withPresentableText(it.name)
                .withTypeText(it.type)
                .withCaseSensitivity(true)
                .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
              completionResultSet.addElement(builder)
            }
          }
        }
      }
    }

    private fun addBeanNameLookupItems(psiElement: PsiElement, set: CompletionResultSet) {
      val project = psiElement.project
      val psiFiles = getAllFiles(project)
      val ob = psiElement.ancestors.firstOrNull { it is JsonObject }
      psiFiles.forEach {
        val psiClassFullName = getPsiClassName(it)
        val name = it.name.substring(0, it.name.lastIndexOf("."))
        val builder = LookupElementBuilder
          .create(name)
          .withBoldness(true)
          .withPresentableText(psiClassFullName)
          .withCaseSensitivity(true)
          .withInsertHandler { context, el ->
            if (ob is JsonObject) {
              WriteCommandAction.runWriteCommandAction(project) {
                val generator = JsonElementGenerator(project)
                for (i in 0..context.file.endOffset) {
                  val elm = context.file.findElementAt(i)!!
                  val p = elm.parent
                  if (p is JsonProperty && p.name == el.lookupString) {
                    if (p.children.none { it is JsonObject }) {
                      if (p.children.size >= 2) {
//                        p.addAfter(generator.createObject("""{"${BEAN_PROPERTY}":"$psiClassFullName",
//"includes":[]}"""), p.children[0])
                      }
                    }
                  }
                  if (p != null && p is JsonObject
                    && p.parent is JsonProperty
                    && (p.parent as JsonProperty).name == el.lookupString) {
                    if (p.findProperty(BEAN_PROPERTY) != null) {
                      p.findProperty(BEAN_PROPERTY)!!.delete()
                    }
                    JsonPsiUtil.addProperty(p, generator.createProperty(BEAN_PROPERTY, "\"" + psiClassFullName + "\""), true)
                    break
                  }
                }
              }
            }
          }
          .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
        set.addElement(builder)
      }
    }

    private fun getPsiClassName(it: PsiFile): String {
      return if (it is KtFile) {
        it.packageFqName.asString() + ".${it.classes[0].name}"
      } else if (it is PsiJavaFile) {
        if (it.classes.isNotEmpty()) {
          it.packageName + ".${it.classes[0].name}"
        } else {
          ""
        }
      } else {
        it as PsiClassOwner
        "${it.classes[0].name}"
      }
    }

    private fun getAllFiles(project: Project): List<PsiFile> {
      val sourceRoots = ProjectRootManager.getInstance(project).contentSourceRoots
      val psiFiles = ArrayList<PsiFile>()
      sourceRoots.forEach {
        if (it is VirtualDirectoryImpl) {
          it.cachedPosterity.forEach {
            val f = PsiManager.getInstance(project).findFile(it)
            if (f != null && (f.language.displayName == "Kotlin" || f.language.displayName == "Java")
              && !(f.name.endsWith("DAO") || f.name.endsWith("Service") || f.name.endsWith("Controller")
              || f.name.endsWith("Impl"))) {
              psiFiles.add(f)
            }
          }
        }
      }
      return psiFiles
    }
  }

  class FieldObject(val name: String, val type: String)

  private fun getFieldNames(psiFile: PsiFile, existsValues: List<String>): ArrayList<FieldObject> {
    val fields = ArrayList<FieldObject>()
    if (psiFile is PsiClassOwner) {
      if (psiFile.classes.isNotEmpty()) {
        psiFile.classes[0]
          .allMethods.filter {
          it.parameterList.parametersCount == 0 && (it.name.startsWith("get") || it.name
            .startsWith("is") && it.returnType != null)
        }.forEach {
          val fieldName = resolveFieldNameFromMethodName(it.name)
          if (fieldName !in existsValues && fieldName.isNotBlank() && fieldName != "class") {
            fields.add(FieldObject(fieldName, it.returnType.toString().replace("PsiType:", "")))
          }
        }
      }
    }
    return fields
  }

  private fun resolveFieldNameFromMethodName(name: String): String {
    if (name.startsWith("get")) {
      val n = name.replaceFirst("get", "")
      return n.substring(0, 1).toLowerCase() + n.substring(1)
    } else if (name.startsWith("is")) {
      val n = name.replaceFirst("is", "")
      return n.substring(0, 1).toLowerCase() + n.substring(1)
    }
    return ""
  }

  private val VirtualDirectoryImpl.cachedPosterity: ArrayList<VirtualFile>
    get() {
      val list = ArrayList<VirtualFile>()
      getCachedChildren(list, this)
      return list
    }

  private fun getCachedChildren(list: ArrayList<VirtualFile>, virtualDirectoryImpl: VirtualDirectoryImpl) {
    list.addAll(virtualDirectoryImpl.cachedChildren.filter { !it.isDirectory })
    virtualDirectoryImpl.cachedChildren.filter { it is VirtualDirectoryImpl }
      .forEach {
        getCachedChildren(list, it as VirtualDirectoryImpl)
      }
  }

  companion object {
    const val BEAN_PROPERTY = "@bean"
  }
}

