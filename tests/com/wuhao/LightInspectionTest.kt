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
import com.intellij.testFramework.InspectionTestUtil
import com.intellij.testFramework.builders.ModuleFixtureBuilder
import com.intellij.testFramework.createGlobalContextForTool
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import com.wuhao.code.check.inspection.JavaFormatInspection
import com.wuhao.code.check.inspection.KotlinFormatInspection
import java.io.File
import java.util.*
import com.intellij.util.containers.ContainerUtil
import com.intellij.codeInsight.intention.IntentionAction
import junit.framework.TestCase


/**
 *
 * @author 吴昊
 * @since 1.2.6
 */
class LightInspectionTest : CodeInsightFixtureTestCase<ModuleFixtureBuilder<*>>() {

  override fun getBasePath(): String {
    return File("").absolutePath
  }

  override fun getHomePath(): String {
    return ""
  }

  override fun setUp() {
    Locale.setDefault(Locale.ENGLISH)
    System.setProperty(PathManager.PROPERTY_HOME_PATH, homePath)
    super.setUp()
  }

  fun testAllJava() {
    doTestGlobalInspection("testData", JavaFormatInspection())
  }

  fun testAllKotlin() {
    doTestGlobalInspection("testData", KotlinFormatInspection())
  }

  fun testJavaInspection() {
    val inspector = JavaFormatInspection()
    myFixture.configureByFile(BASE_PATH + "JavaExample.java")
    myFixture.enableInspections(inspector)
    myFixture.testHighlighting(true, false, true)
  }

  fun testJavaInterfaceInspection() {
    val inspector = JavaFormatInspection()
    myFixture.configureByFile(BASE_PATH + "JavaInterfaceExample.java")
    myFixture.enableInspections(inspector)
    myFixture.testHighlighting(true, false, true)
  }

  fun testJavaWhiteSpace() {
    val inspector = JavaFormatInspection()
    myFixture.configureByFile(BASE_PATH + "WhiteSpace.java")
    myFixture.enableInspections(inspector)
    myFixture.testHighlighting(true, false, true)
  }

  fun testKotlinInspection() {
    val inspector = KotlinFormatInspection()
    myFixture.configureByFile(BASE_PATH + "KtWhiteSpace.kt")
    myFixture.enableInspections(inspector)
    myFixture.testHighlighting(true, false, true)
  }

  fun testKotlinInterfaceInspection() {
    val inspector = KotlinFormatInspection()
    myFixture.configureByFile(BASE_PATH + "InterfaceExample.kt")
    myFixture.enableInspections(inspector)
    myFixture.testHighlighting(true, false, true)
    applySingleQuickFix("添加注释")
  }

  protected fun applySingleQuickFix(quickFixName: String) {
    val availableIntentions = myFixture.filterAvailableIntentions(quickFixName)
    myFixture.availableIntentions.forEach {
      println("${it.familyName}/${it.javaClass}")
    }
    availableIntentions.forEach {
      println(it)
    }
    val action = ContainerUtil.getFirstItem(availableIntentions)

//    TestCase.assertNotNull(action)
//    myFixture.launchAction(action!!)
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

  private fun doTestGlobalInspection(testDir: String, inspection: LocalInspectionTool) {
    val problemDescriptors = getGlobalInspectionResults(testDir, inspection)
    for (problem in problemDescriptors) {
      if (problem is ProblemDescriptorBase) {
        println(buildProblemMessage(problem))
      }
    }
  }

  private fun getGlobalInspectionResults(testDir: String, inspection: LocalInspectionTool): Collection<CommonProblemDescriptor> {
    val toolWrapper = LocalInspectionToolWrapper(inspection)
    myFixture.testDataPath = testDir
    val sourceDir = myFixture.copyDirectoryToProject("src", "src")
    val psiDirectory = myFixture.psiManager.findDirectory(sourceDir)
        ?: throw AssertionError("Could not find $sourceDir")
    val scope = AnalysisScope(psiDirectory)
    scope.invalidate()
    val globalContext = createGlobalContextForTool(scope, project, listOf(toolWrapper))
    InspectionTestUtil.runTool(toolWrapper, scope, globalContext)
    (myFixture.tempDirFixture as? LightTempDirTestFixtureImpl)?.deleteAll()
    return globalContext.getPresentation(toolWrapper).problemDescriptors
  }

  companion object {

    const val BASE_PATH = "testData/src/error/"

  }

}

