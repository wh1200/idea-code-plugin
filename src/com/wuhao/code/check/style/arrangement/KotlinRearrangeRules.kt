/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement

import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.BY_NAME
import com.wuhao.code.check.style.EntryType.CLASS
import com.wuhao.code.check.style.EntryType.CONSTRUCTOR
import com.wuhao.code.check.style.EntryType.DATA_CLASS
import com.wuhao.code.check.style.EntryType.FUNCTION
import com.wuhao.code.check.style.EntryType.INTERFACE
import com.wuhao.code.check.style.EntryType.OBJECT
import com.wuhao.code.check.style.EntryType.PROPERTY
import com.wuhao.code.check.style.KotlinModifier.INNER
import com.wuhao.code.check.style.KotlinModifier.INTERNAL
import com.wuhao.code.check.style.KotlinModifier.LATEINIT
import com.wuhao.code.check.style.KotlinModifier.OPEN
import com.wuhao.code.check.style.KotlinModifier.PRIVATE
import com.wuhao.code.check.style.KotlinModifier.PROTECTED

/**
 * kotlin代码排序规则列表
 * @author 吴昊
 * @since 1.3.1
 */
object KotlinRearrangeRules : BaseRules() {

  override fun get(): List<RuleDescription> {
    return listOf(
        RuleDescription(listOf(PROPERTY, OPEN), BY_NAME),
        RuleDescription(listOf(PROPERTY, OPEN, INTERNAL), BY_NAME),
        RuleDescription(listOf(PROPERTY, OPEN, PROTECTED), BY_NAME),
        RuleDescription(listOf(PROPERTY, OPEN, PRIVATE), BY_NAME),
        RuleDescription(listOf(PROPERTY, OPEN, LATEINIT), BY_NAME),
        RuleDescription(listOf(PROPERTY, OPEN, INTERNAL, LATEINIT), BY_NAME),
        RuleDescription(listOf(PROPERTY, OPEN, PROTECTED, LATEINIT), BY_NAME),
        RuleDescription(listOf(PROPERTY, OPEN, PRIVATE, LATEINIT), BY_NAME),
        RuleDescription(PROPERTY, BY_NAME),
        RuleDescription(listOf(PROPERTY, INTERNAL), BY_NAME),
        RuleDescription(listOf(PROPERTY, PROTECTED), BY_NAME),
        RuleDescription(listOf(PROPERTY, PRIVATE), BY_NAME),
        RuleDescription(listOf(PROPERTY, LATEINIT), BY_NAME),
        RuleDescription(listOf(PROPERTY, INTERNAL, LATEINIT), BY_NAME),
        RuleDescription(listOf(PROPERTY, PROTECTED, LATEINIT), BY_NAME),
        RuleDescription(listOf(PROPERTY, PRIVATE, LATEINIT), BY_NAME),
        RuleDescription(CONSTRUCTOR, BY_NAME),

        RuleDescription(listOf(FUNCTION, OPEN), BY_NAME),
        RuleDescription(listOf(FUNCTION, OPEN, INTERNAL), BY_NAME),
        RuleDescription(listOf(FUNCTION, OPEN, PROTECTED), BY_NAME),
        RuleDescription(listOf(FUNCTION, OPEN, PRIVATE), BY_NAME),
        RuleDescription(listOf(FUNCTION, OPEN, LATEINIT), BY_NAME),
        RuleDescription(listOf(FUNCTION, OPEN, INTERNAL, LATEINIT), BY_NAME),
        RuleDescription(listOf(FUNCTION, OPEN, PROTECTED, LATEINIT), BY_NAME),
        RuleDescription(listOf(FUNCTION, OPEN, PRIVATE, LATEINIT), BY_NAME),
        RuleDescription(FUNCTION, BY_NAME),
        RuleDescription(listOf(FUNCTION, INTERNAL), BY_NAME),
        RuleDescription(listOf(FUNCTION, PROTECTED), BY_NAME),
        RuleDescription(listOf(FUNCTION, PRIVATE), BY_NAME),
        RuleDescription(listOf(FUNCTION, LATEINIT), BY_NAME),
        RuleDescription(listOf(FUNCTION, INTERNAL, LATEINIT), BY_NAME),
        RuleDescription(listOf(FUNCTION, PROTECTED, LATEINIT), BY_NAME),
        RuleDescription(listOf(FUNCTION, PRIVATE, LATEINIT), BY_NAME),
        RuleDescription(INNER, BY_NAME),
        RuleDescription(INTERFACE, BY_NAME),
        RuleDescription(OBJECT, BY_NAME),
        RuleDescription(DATA_CLASS, BY_NAME),
        RuleDescription(CLASS, BY_NAME))
  }
}

