/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement

import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens
import com.wuhao.code.check.PostStart
import com.wuhao.code.check.style.EntryType
import com.wuhao.code.check.style.KotlinModifier

/**
 * kotlin代码排序规则列表
 * @author 吴昊
 * @since 1.3.1
 */
object KotlinRearrangeRules : Rules() {

  override fun get(): List<PostStart.RuleDescription> {
    return listOf(
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.OPEN), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.OPEN, KotlinModifier.INTERNAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.OPEN, KotlinModifier.PROTECTED), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.OPEN, KotlinModifier.PRIVATE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.OPEN, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.OPEN, KotlinModifier.INTERNAL, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.OPEN, KotlinModifier.PROTECTED, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.OPEN, KotlinModifier.PRIVATE, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.INTERNAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.PROTECTED), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.PRIVATE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.INTERNAL, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.PROTECTED, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.PROPERTY, KotlinModifier.PRIVATE, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.CONSTRUCTOR), StdArrangementTokens.Order.BY_NAME),

        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.OPEN), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.OPEN, KotlinModifier.INTERNAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.OPEN, KotlinModifier.PROTECTED), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.OPEN, KotlinModifier.PRIVATE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.OPEN, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.OPEN, KotlinModifier.INTERNAL, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.OPEN, KotlinModifier.PROTECTED, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.OPEN, KotlinModifier.PRIVATE, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.INTERNAL), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.PROTECTED), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.PRIVATE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.INTERNAL, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.PROTECTED, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.FUNCTION, KotlinModifier.PRIVATE, KotlinModifier.LATEINIT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(KotlinModifier.INNER), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.INTERFACE), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.OBJECT), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.DATA_CLASS), StdArrangementTokens.Order.BY_NAME),
        PostStart.RuleDescription(listOf(EntryType.CLASS), StdArrangementTokens.Order.BY_NAME))
  }

}

