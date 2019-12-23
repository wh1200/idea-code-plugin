/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.CommonProblemDescriptor
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptorBase
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.application.PathManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.testFramework.InspectionTestUtil
import com.intellij.testFramework.builders.ModuleFixtureBuilder
import com.intellij.testFramework.createGlobalContextForTool
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import com.wuhao.code.check.constants.Messages.DO_NOT_MATCH_ROOT_PATH
import com.wuhao.code.check.constants.Messages.MISSING_REQUEST_MAPPING_ANNOTATION
import com.wuhao.code.check.constants.Messages.USE_LOG_INSTEAD_OF_PRINT
import com.wuhao.code.check.inspection.inspections.JavaCommentInspection
import com.wuhao.code.check.inspection.inspections.JavaFormatInspection
import com.wuhao.code.check.inspection.inspections.KotlinActionSpecificationInspection
import com.wuhao.code.check.inspection.inspections.KotlinFormatInspection
import java.io.File

/**
 *
 * @author 吴昊
 * @since 1.2.6
 */
class LightInspectionTest : CodeInsightFixtureTestCase<ModuleFixtureBuilder<*>>() {

  override fun getHomePath(): String {
    return ""
  }

  override fun setUp() {
    System.setProperty(PathManager.PROPERTY_HOME_PATH, homePath)
    super.setUp()
  }

  fun testAllJava() {
    val problems = doTestGlobalInspection("src", JavaFormatInspection())
    problems.assertProblem("JavaExample.java", USE_LOG_INSTEAD_OF_PRINT)
    problems.assertProblem("ExampleController.java", MISSING_REQUEST_MAPPING_ANNOTATION)
    problems.assertProblem("ExampleController2.java", MISSING_REQUEST_MAPPING_ANNOTATION)
    problems.assertProblem("ExampleController3.java", DO_NOT_MATCH_ROOT_PATH)
  }

  fun testAllKotlin() {
    doTestGlobalInspection("src", KotlinFormatInspection())
  }

  fun testApiSpecification() {
    val inspections = doTestGlobalInspection("src/error/mvc",
        KotlinActionSpecificationInspection())
    println(inspections.size)
  }

  fun testJavaInspection() {
    doTestGlobalInspection("src/error/JavaExample.java", JavaFormatInspection())
  }

  fun testJavaInterfaceInspection() {
    doJavaInspectionTest("src/error/java")
  }

  fun testJavaTypeArgument() {
    doJavaInspectionTest("src/error/java")
  }

  fun testJavaWhiteSpace() {
    val inspector = JavaFormatInspection()
    myFixture.configureByFile(basePath + "WhiteSpace.java")
    myFixture.enableInspections(inspector)
    myFixture.testHighlighting(true, false, true)
  }

  fun testMissingRequestMapping() {
    doJavaInspectionTest("error/java/ExampleController.java")
  }

  fun testMissingReuqestMapping2() {
    doJavaInspectionTest("error/java/ExampleController2.java")
  }

  fun testKotlinInspection() {
    doKotlinInspectionTest("error/KtWhiteSpace.kt")
  }

  fun testKotlinInterfaceInspection() {
    doKotlinInspectionTest("error/InterfaceExample.kt")
    applySingleQuickFix("添加注释")
  }

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

  private fun buildProblemMessage(problem: ProblemDescriptorBase): String {

    return """------------------------------
| 文件: ${problem.psiElement.containingFile.name}
| 路径: ${problem.psiElement.containingFile.virtualFile.path}
| 元素: ${problem.psiElement.text}
| 行号: ${problem.lineNumber}
| 描述: ${problem.descriptionTemplate}
| 类型: ${problem.highlightType}
------------------------------""".trimIndent()
  }

  private fun doInspectionTest(path: String, inspector: LocalInspectionTool) {
    myFixture.configureByFile(resolvePath(path))
    myFixture.enableInspections(inspector)
    myFixture.testHighlighting(true, false, true)
  }

  private fun resolvePath(path: String): String {
    return (basePath + path).replace("/", "\\\\")
  }

  private fun doJavaInspectionTest(path: String) {
    doInspectionTest(path, JavaFormatInspection())
    doInspectionTest(path, JavaCommentInspection())
  }

  private fun doKotlinInspectionTest(path: String) {
    val inspector = KotlinFormatInspection()
    doInspectionTest(path, inspector)
  }

  private fun doTestGlobalInspection(path: String, inspection: LocalInspectionTool): Collection<ProblemDescriptorBase> {
    val problemDescriptors = getGlobalInspectionResults(path, inspection)
        .map { it as ProblemDescriptorBase }
    for (problem in problemDescriptors) {
      println(buildProblemMessage(problem))
    }
    return problemDescriptors
  }

  private fun getGlobalInspectionResults(path: String, inspection: LocalInspectionTool): Collection<CommonProblemDescriptor> {
    val toolWrapper = LocalInspectionToolWrapper(inspection)
    myFixture.testDataPath = "testData"
    val file = File(myFixture.testDataPath + File.separator + path)
    println("File: $file")
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

}

private fun Collection<ProblemDescriptorBase>.assertProblem(file: String, description: String) {
  val problems = this.filter { it.psiElement.containingFile.name == file }
      .map { it.descriptionTemplate }
  if (!problems.contains(description)) {
    throw IllegalStateException("Missing problem " + description + " in file $file")
  }
}
