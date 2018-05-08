/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionTool
import com.siyeh.ig.LightInspectionTestCase

/**
 *
 * @author 吴昊
 * @since 1.2.6
 */
class LightInspectionTest : LightInspectionTestCase() {

  override fun setUp() {
    super.setUp()
  }

  override fun getTestDataPath(): String {
    return "testData"
  }

  override fun getInspection(): InspectionProfileEntry? {
    return Test()
  }

  fun testInspection() {
  }
}

/**
 *
 * @author 吴昊
 * @since
 */
class Test: LocalInspectionTool() {

}

