/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao

import com.wuhao.code.check.constants.Messages.DO_NOT_MATCH_ROOT_PATH
import com.wuhao.code.check.constants.Messages.MISSING_REQUEST_MAPPING_ANNOTATION
import com.wuhao.code.check.constants.Messages.USE_LOG_INSTEAD_OF_PRINT
import com.wuhao.code.check.inspection.inspections.JavaCommentInspection
import com.wuhao.code.check.inspection.inspections.JavaFormatInspection
import com.wuhao.code.check.inspection.inspections.KotlinActionSpecificationInspection
import com.wuhao.code.check.inspection.inspections.KotlinFormatInspection

/**
 *
 * @author 吴昊
 * @since 1.2.6
 */
class LightInspectionTest : BaseLightInspectionTest() {

  fun testAllJava() {
    val problems = doTestGlobalInspection("src", JavaFormatInspection())
    problems.assertNoProblem("FILEName.java")
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


  private fun doJavaInspectionTest(path: String) {
    doInspectionTest(path, JavaFormatInspection())
    doInspectionTest(path, JavaCommentInspection())
  }

  private fun doKotlinInspectionTest(path: String) {
    val inspector = KotlinFormatInspection()
    doInspectionTest(path, inspector)
  }


}

