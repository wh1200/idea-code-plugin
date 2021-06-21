/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.CommonProblemDescriptor
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptorBase
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.testFramework.InspectionTestUtil
import com.intellij.testFramework.builders.ModuleFixtureBuilder
import com.intellij.testFramework.createGlobalContextForTool
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import java.io.File

fun Collection<ProblemDescriptorBase>.assertNoProblem(file: String) {
  if (this.any { it.psiElement.containingFile.name == file }) {
    throw IllegalStateException("File $file should not has any problems")
  }
}

fun Collection<ProblemDescriptorBase>.assertProblem(file: String, description: String) {
  val problems = this.filter { it.psiElement.containingFile.name == file }
      .map { it.descriptionTemplate }
  if (!problems.contains(description)) {
    for (problem in this) {
      println(buildProblemMessage(problem))
    }
    throw IllegalStateException("Missing problem $description in file $file")
  }
}

fun buildProblemMessage(problem: ProblemDescriptorBase): String {
  return """------------------------------
| 文件: ${problem.psiElement.containingFile.name}
| 路径: ${problem.psiElement.containingFile.virtualFile.path}
| 元素: ${problem.psiElement.text}
| 行号: ${problem.lineNumber}
| 描述: ${problem.descriptionTemplate}
| 类型: ${problem.highlightType}
------------------------------""".trimIndent()
}

/**
 *
 * @author 吴昊
 * @since 1.2.6
 */
open class BaseLightInspectionTest : CodeInsightFixtureTestCase<ModuleFixtureBuilder<*>>() {

  protected fun applySingleQuickFix(quickFixName: String) {
    myFixture.project
    val availableIntentions = myFixture.filterAvailableIntentions(quickFixName)
    myFixture.availableIntentions.forEach {
      println("${it.familyName}/${it.javaClass}")
    }
    availableIntentions.forEach {
      println(it)
    }
  }

  protected fun doInspectionTest(path: String, inspector: LocalInspectionTool) {
    myFixture.configureByFile(resolvePath(path))
    myFixture.enableInspections(inspector)
    myFixture.testHighlighting(true, false, true)
  }

  protected fun doTestGlobalInspection(path: String, inspection: LocalInspectionTool): Collection<ProblemDescriptorBase> {
    val problemDescriptors = getGlobalInspectionResults(path, inspection)
        .map { it as ProblemDescriptorBase }
    for (problem in problemDescriptors) {
      println(buildProblemMessage(problem))
    }
    return problemDescriptors
  }

  protected fun getGlobalInspectionResults(path: String, inspection: LocalInspectionTool): Collection<CommonProblemDescriptor> {
    val toolWrapper = LocalInspectionToolWrapper(inspection)
    myFixture.testDataPath = File("").absolutePath + "/testData"
    val file = File(myFixture.testDataPath + File.separator + path)
    println("File: ${file.absolutePath}")
    val sourceDir = if (file.isFile) {
      myFixture.psiManager.findFile(myFixture.copyFileToProject(path, path.replace(file.name, "")))
          ?: throw AssertionError("Could not find $file")
    } else {
      myFixture.psiManager.findDirectory(myFixture.copyDirectoryToProject(path, path))
          ?: throw AssertionError("Could not find $file")
    }
    println("Source directory: $sourceDir")
    val scope = if (sourceDir is PsiDirectory) {
      AnalysisScope(sourceDir)
    } else {
      AnalysisScope(sourceDir as PsiFile)
    }
    scope.invalidate()
    val globalContext = createGlobalContextForTool(scope, project, listOf(toolWrapper))
    InspectionTestUtil.runTool(toolWrapper, scope, globalContext)
    (myFixture.tempDirFixture as? LightTempDirTestFixtureImpl)?.deleteAll()
    return globalContext.getPresentation(toolWrapper).problemDescriptors
  }

  protected fun resolvePath(path: String): String {
    return (basePath + path).replace("/", "\\\\")
  }

}
