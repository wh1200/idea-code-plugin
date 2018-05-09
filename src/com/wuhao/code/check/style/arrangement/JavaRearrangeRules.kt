/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement

import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens
import com.wuhao.code.check.PostStart

/**
 *
 * @author 吴昊
 * @since 1.3.1
 */
object JavaRearrangeRules: Rules() {

  override fun get(): List<PostStart.RuleDescription> {
    return listOf(
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PUBLIC, StdArrangementTokens.Modifier.STATIC, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PROTECTED, StdArrangementTokens.Modifier.STATIC, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PACKAGE_PRIVATE, StdArrangementTokens.Modifier.STATIC, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PRIVATE, StdArrangementTokens.Modifier.STATIC, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PUBLIC, StdArrangementTokens.Modifier.STATIC), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PROTECTED, StdArrangementTokens.Modifier.STATIC), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PACKAGE_PRIVATE, StdArrangementTokens.Modifier.STATIC), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PRIVATE, StdArrangementTokens.Modifier.STATIC), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.INIT_BLOCK, StdArrangementTokens.Modifier.STATIC)),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PUBLIC, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PROTECTED, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PACKAGE_PRIVATE, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PRIVATE, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),

        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PUBLIC), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PROTECTED), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PACKAGE_PRIVATE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD, StdArrangementTokens.Modifier.PRIVATE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.FIELD), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.INIT_BLOCK)),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.CONSTRUCTOR)),

        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PUBLIC, StdArrangementTokens.Modifier.STATIC, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PACKAGE_PRIVATE, StdArrangementTokens.Modifier.STATIC, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PROTECTED, StdArrangementTokens.Modifier.STATIC, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PRIVATE, StdArrangementTokens.Modifier.STATIC, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),

        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PUBLIC, StdArrangementTokens.Modifier.STATIC), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PACKAGE_PRIVATE, StdArrangementTokens.Modifier.STATIC), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PROTECTED, StdArrangementTokens.Modifier.STATIC), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PRIVATE, StdArrangementTokens.Modifier.STATIC), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PUBLIC, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PACKAGE_PRIVATE, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PROTECTED, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PRIVATE, StdArrangementTokens.Modifier.FINAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PUBLIC), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PACKAGE_PRIVATE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PROTECTED), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD, StdArrangementTokens.Modifier.PRIVATE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.METHOD), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.ENUM), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.INTERFACE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.CLASS, StdArrangementTokens.Modifier.STATIC), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(StdArrangementTokens.EntryType.CLASS, StdArrangementTokens.EntryType.CLASS), StdArrangementTokens.Order.BY_NAME))
  }
}


