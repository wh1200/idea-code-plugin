/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement

import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.*
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Modifier.*
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.BY_NAME

/**
 *
 * @author 吴昊
 * @since 1.3.1
 */
object JavaRearrangeRules: BaseRules() {

  override fun get(): List<RuleDescription> {
    return listOf(
        RuleDescription(listOf(FIELD, PUBLIC, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PROTECTED, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PACKAGE_PRIVATE, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PRIVATE, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PUBLIC, STATIC), BY_NAME),
        RuleDescription(listOf(FIELD, PROTECTED, STATIC), BY_NAME),
        RuleDescription(listOf(FIELD, PACKAGE_PRIVATE, STATIC), BY_NAME),
        RuleDescription(listOf(FIELD, PRIVATE, STATIC), BY_NAME),
        RuleDescription(listOf(INIT_BLOCK, STATIC)),
        RuleDescription(listOf(FIELD, PUBLIC, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PROTECTED, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PACKAGE_PRIVATE, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PRIVATE, FINAL), BY_NAME),

        RuleDescription(listOf(FIELD, PUBLIC), BY_NAME),
        RuleDescription(listOf(FIELD, PROTECTED), BY_NAME),
        RuleDescription(listOf(FIELD, PACKAGE_PRIVATE), BY_NAME),
        RuleDescription(listOf(FIELD, PRIVATE), BY_NAME),
        RuleDescription(FIELD, BY_NAME),
        RuleDescription(INIT_BLOCK),
        RuleDescription(CONSTRUCTOR),

        RuleDescription(listOf(METHOD, PUBLIC, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PACKAGE_PRIVATE, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PROTECTED, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PRIVATE, STATIC, FINAL), BY_NAME),

        RuleDescription(listOf(METHOD, PUBLIC, STATIC), BY_NAME),
        RuleDescription(listOf(METHOD, PACKAGE_PRIVATE, STATIC), BY_NAME),
        RuleDescription(listOf(METHOD, PROTECTED, STATIC), BY_NAME),
        RuleDescription(listOf(METHOD, PRIVATE, STATIC), BY_NAME),
        RuleDescription(listOf(METHOD, PUBLIC, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PACKAGE_PRIVATE, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PROTECTED, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PRIVATE, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PUBLIC), BY_NAME),
        RuleDescription(listOf(METHOD, PACKAGE_PRIVATE), BY_NAME),
        RuleDescription(listOf(METHOD, PROTECTED), BY_NAME),
        RuleDescription(listOf(METHOD, PRIVATE), BY_NAME),
        RuleDescription(METHOD, BY_NAME),
        RuleDescription(ENUM, BY_NAME),
        RuleDescription(INTERFACE, BY_NAME),
        RuleDescription(listOf(CLASS, STATIC), BY_NAME),
        RuleDescription(listOf(CLASS, CLASS), BY_NAME))
  }
}


