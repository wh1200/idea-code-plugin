/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement.vue

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.arrangement.ArrangementSettings

/**
 * vue排序访问器
 * @author 吴昊
 * @since 1.3.1
 */
class VueArrangementVisitor(private val myInfo: VueArrangementParseInfo,
                            private val myDocument: Document?,
                            private val myRanges: Collection<TextRange>,
                            settings: ArrangementSettings) : VueRecursiveVisitor() {


}

