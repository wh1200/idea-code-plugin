/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.processors

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.openapi.editor.GutterMarkPreprocessor

class MyBatisGutterProcessor : GutterMarkPreprocessor {

  override fun processMarkers(list: MutableList<GutterMark>): List<GutterMark> {
    return list
  }
}
