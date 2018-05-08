/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.actions.LastRunReformatCodeOptionsProvider
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.arrangement.group.ArrangementGroupingRule
import com.intellij.psi.codeStyle.arrangement.match.ArrangementSectionRule
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementEntryMatcher
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementMatchRule
import com.intellij.psi.codeStyle.arrangement.model.ArrangementAtomMatchCondition
import com.intellij.psi.codeStyle.arrangement.model.ArrangementCompositeMatchCondition
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementExtendableSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementRuleAliasToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.*
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Grouping.*
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Modifier.*
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.*
import com.intellij.psi.css.CssFileType
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.vuejs.VueFileType

/**
 * 项目启动时运行
 * @author 吴昊
 * @since 1.2.6
 */
class PostStart : StartupActivity {

  override fun runActivity(project: Project) {
    // 强制启用java代码重排和import重新组织的功能
    val myLastRunSettings = LastRunReformatCodeOptionsProvider(PropertiesComponent.getInstance())
    myLastRunSettings.saveRearrangeCodeState(true)
    myLastRunSettings.saveRearrangeState(JavaLanguage.INSTANCE, true)
    myLastRunSettings.saveRearrangeState(KotlinLanguage.INSTANCE, true)
    myLastRunSettings.saveOptimizeImportsState(true)
    // 设定java代码重排规则
    val settings = CodeStyle.getSettings(project)
    settings.getCommonSettings(JavaLanguage.INSTANCE).apply {
      setArrangementSettings(createSettings())
    }
    settings.getCommonSettings(KotlinLanguage.INSTANCE).apply {
      setArrangementSettings(createSettings())
    }
    // 设定代码缩进
    setIndent(settings)
  }

  private fun setIndent(settings: CodeStyleSettings) {
    val setIndentFileTypes = listOf(
        JavaFileType.INSTANCE, KotlinFileType.INSTANCE, JavaScriptFileType.INSTANCE,
        TypeScriptFileType.INSTANCE, VueFileType.INSTANCE,
        XmlFileType.INSTANCE, CssFileType.INSTANCE
    )
    setIndentFileTypes.forEach { fileType ->
      settings.getIndentOptions(fileType)
          .apply {
            INDENT_SIZE = DEFAULT_INDENT_SPACE_COUNT
            CONTINUATION_INDENT_SIZE = DEFAULT_CONTINUATION_INDENT_SPACE_COUNT
            TAB_SIZE = DEFAULT_INDENT_SPACE_COUNT
            USE_TAB_CHARACTER = false
          }
    }
  }

  private fun createSettings(): StdArrangementSettings {
    val groupingRules = listOf(
        ArrangementGroupingRule(GETTERS_AND_SETTERS, KEEP),
        ArrangementGroupingRule(OVERRIDDEN_METHODS, BY_NAME),
        ArrangementGroupingRule(DEPENDENT_METHODS, BREADTH_FIRST)
    )
    val sections = getRules().map { rule ->
      if (rule.order == null) {
        StdArrangementMatchRule(createMatcher(rule), BY_NAME)
      } else {
        StdArrangementMatchRule(createMatcher(rule))
      }
    }.map {
      ArrangementSectionRule.create(it)
    }
    val tokens = listOf(StdArrangementRuleAliasToken("visibility").apply {
      definitionRules = listOf(PUBLIC, PACKAGE_PRIVATE, PROTECTED, PRIVATE).map {
        StdArrangementMatchRule(
            StdArrangementEntryMatcher(ArrangementAtomMatchCondition(it))
        )
      }
    })
    return StdArrangementExtendableSettings(groupingRules, sections, tokens)
  }

  private fun createMatcher(rule: PostStart.RuleDescription): StdArrangementEntryMatcher {
    return StdArrangementEntryMatcher(
        ArrangementCompositeMatchCondition().apply {
          rule.template.forEach { token ->
            this.addOperand(ArrangementAtomMatchCondition(token))
          }
        }
    )
  }

  private fun getRules(): List<RuleDescription> {
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
        RuleDescription(listOf(FIELD), BY_NAME),
        RuleDescription(listOf(INIT_BLOCK)),
        RuleDescription(listOf(CONSTRUCTOR)),

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
        RuleDescription(listOf(METHOD), BY_NAME),
        RuleDescription(listOf(ENUM), BY_NAME),
        RuleDescription(listOf(INTERFACE), BY_NAME),
        RuleDescription(listOf(CLASS, STATIC), BY_NAME),
        RuleDescription(listOf(CLASS, CLASS), BY_NAME))
  }

  /**
   * java代码排序规则描述
   * @author 吴昊
   * @since 1.2.6
   */
  private class RuleDescription(val template: List<ArrangementSettingsToken>) {

    var order: ArrangementSettingsToken? = null

    constructor(template: List<ArrangementSettingsToken>, order: ArrangementSettingsToken)
        : this(template) {
      this.order = order
    }
  }
}

