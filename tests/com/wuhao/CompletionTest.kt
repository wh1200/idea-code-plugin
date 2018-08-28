/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao

import com.intellij.codeInsight.completion.LightCompletionTestCase
import java.util.*

/**
 *
 * @author 吴昊
 * @since
 */
class CompletionTest : LightCompletionTestCase() {

  override fun getTestDataPath(): String {
    return "testData"
  }

  override fun setUp() {
    Locale.setDefault(Locale.ENGLISH)
    super.setUp()
  }

  fun test() {
  }

}

