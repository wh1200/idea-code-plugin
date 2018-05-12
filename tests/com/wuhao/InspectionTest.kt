/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao

import com.intellij.codeInspection.InspectionProfileEntry
import com.siyeh.ig.LightInspectionTestCase
import com.wuhao.code.check.inspection.CodeFormatInspection

/**
 *
 * @author 吴昊
 * @since 1.2.6
 */
class InspectionTest : LightInspectionTestCase() {

  override fun getInspection(): InspectionProfileEntry? {
    return CodeFormatInspection()
  }

  override fun getTestDataPath(): String {
    return "testData/Test.kt"
  }

  fun testInspection() {
  }

}


