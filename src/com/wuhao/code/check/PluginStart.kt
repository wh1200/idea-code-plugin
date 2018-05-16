/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.actions.LastRunReformatCodeOptionsProvider
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.intellij.psi.codeStyle.arrangement.group.ArrangementGroupingRule
import com.intellij.psi.codeStyle.arrangement.match.ArrangementSectionRule
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementEntryMatcher
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementMatchRule
import com.intellij.psi.codeStyle.arrangement.model.ArrangementAtomMatchCondition
import com.intellij.psi.codeStyle.arrangement.model.ArrangementCompositeMatchCondition
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementExtendableSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementRuleAliasToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Grouping.*
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Modifier.*
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.*
import com.intellij.psi.css.CssFileType
import com.wuhao.code.check.style.KotlinModifier.LATEINIT
import com.wuhao.code.check.style.KotlinModifier.OPEN
import com.wuhao.code.check.style.arrangement.JavaRearrangeRules
import com.wuhao.code.check.style.arrangement.KotlinRearrangeRules
import com.wuhao.code.check.style.arrangement.RuleDescription
import com.wuhao.code.check.style.arrangement.VueRearrangeRules
import com.wuhao.code.check.template.KotlinTemplates
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.VueLanguage

/**
 * 项目启动时运行，主要对代码格式的配置按公司规范进行重写
 * @author 吴昊
 * @since 1.2.6
 */
class PluginStart : StartupActivity {

  override fun runActivity(project: Project) {
    // 强制启用java代码重排和import重新组织的功能
    val myLastRunSettings = LastRunReformatCodeOptionsProvider(PropertiesComponent.getInstance())
    myLastRunSettings.saveRearrangeCodeState(true)
    val settings = CodeStyle.getSettings(project)
    myLastRunSettings.saveOptimizeImportsState(true)
    setLanguageArrangeSettings(myLastRunSettings, settings, JavaLanguage.INSTANCE, createJavaSettings())
    setLanguageArrangeSettings(myLastRunSettings, settings, KotlinLanguage.INSTANCE, createKotlinSettings())
    setLanguageArrangeSettings(myLastRunSettings, settings, VueLanguage.INSTANCE, createVueSettings())
    // 设定代码缩进
    setIndent(settings)
    setTemplates(project)
  }

  private fun createJavaSettings(): StdArrangementSettings {
    val groupingRules = listOf(
        ArrangementGroupingRule(GETTERS_AND_SETTERS, KEEP),
        ArrangementGroupingRule(OVERRIDDEN_METHODS, BY_NAME),
        ArrangementGroupingRule(DEPENDENT_METHODS, BREADTH_FIRST)
    )
    val sections = createSections(JavaRearrangeRules.get())
    val tokens = listOf(StdArrangementRuleAliasToken("visibility").apply {
      definitionRules = listOf(PUBLIC, PACKAGE_PRIVATE,
          PROTECTED, PRIVATE, LATEINIT).map {
        StdArrangementMatchRule(
            StdArrangementEntryMatcher(ArrangementAtomMatchCondition(it))
        )
      }
    })
    return StdArrangementExtendableSettings(groupingRules, sections, tokens)
  }

  private fun createKotlinSettings(): StdArrangementSettings {
    val sections = createSections(KotlinRearrangeRules.get())
    val tokens = listOf(StdArrangementRuleAliasToken("visibility").apply {
      definitionRules = listOf(OPEN, PUBLIC, PACKAGE_PRIVATE, PROTECTED, PRIVATE, LATEINIT).map {
        StdArrangementMatchRule(
            StdArrangementEntryMatcher(ArrangementAtomMatchCondition(it))
        )
      }
    })
    return StdArrangementExtendableSettings(listOf(), sections, tokens)
  }

  private fun createMatcher(rule: RuleDescription): StdArrangementEntryMatcher {
    return StdArrangementEntryMatcher(
        ArrangementCompositeMatchCondition().apply {
          rule.template.forEach { token ->
            this.addOperand(ArrangementAtomMatchCondition(token))
          }
        }
    )
  }

  private fun createSections(rules: List<RuleDescription>): List<ArrangementSectionRule> {
    return rules.map { rule ->
      if (rule.order == null) {
        StdArrangementMatchRule(createMatcher(rule), BY_NAME)
      } else {
        StdArrangementMatchRule(createMatcher(rule), rule.order!!)
      }
    }.map {
      ArrangementSectionRule.create(it)
    }
  }

  private fun createVueSettings(): StdArrangementSettings {
    val sections = createSections(VueRearrangeRules.get())
    return StdArrangementExtendableSettings(listOf(), sections, listOf())
  }

  private fun setIndent(settings: CodeStyleSettings) {
    val setIndentFileTypes = listOf(
        JavaFileType.INSTANCE,
        KotlinFileType.INSTANCE,
        JavaScriptFileType.INSTANCE,
        TypeScriptFileType.INSTANCE,
        VueFileType.INSTANCE,
        XmlFileType.INSTANCE,
        CssFileType.INSTANCE
    )
    setIndentFileTypes.forEach { fileType ->
      val language = when (fileType) {
        is JavaFileType -> JavaLanguage.INSTANCE
        is KotlinFileType -> KotlinLanguage.INSTANCE
        is JavaScriptFileType -> JavascriptLanguage.INSTANCE
        is VueFileType -> VueLanguage.INSTANCE
        is CssFileType -> CSSLanguage.INSTANCE
        else -> null
      }

      setIndent(fileType, language, settings)
    }
  }

  private fun setLanguageArrangeSettings(myLastRunSettings: LastRunReformatCodeOptionsProvider,
                                         settings: CodeStyleSettings,
                                         language: Language,
                                         createSettings: StdArrangementSettings) {
    myLastRunSettings.saveRearrangeState(language, true)
    settings.getCommonSettings(language).apply {
      setArrangementSettings(createSettings)
    }
  }

  private fun setTemplates(project: Project) {
    val fileTemplateManager = FileTemplateManager.getInstance(project)
    fileTemplateManager.getInternalTemplate("Kotlin File")?.text = KotlinTemplates.file
    fileTemplateManager.getInternalTemplate("Kotlin Class")?.text = KotlinTemplates.klass
    fileTemplateManager.getInternalTemplate("Kotlin Enum")?.text = KotlinTemplates.enum
    fileTemplateManager.getInternalTemplate("Kotlin Interface")?.text = KotlinTemplates.inter
  }

  companion object {

    fun setIndent(fileType: FileType, language: Language?, settings: CodeStyleSettings) {
      settings.getIndentOptions(fileType).apply {
        INDENT_SIZE = DEFAULT_INDENT_SPACE_COUNT
        CONTINUATION_INDENT_SIZE = DEFAULT_CONTINUATION_INDENT_SPACE_COUNT
        TAB_SIZE = DEFAULT_INDENT_SPACE_COUNT
        USE_TAB_CHARACTER = false
      }
      if (language != null) {
        LanguageCodeStyleSettingsProvider.getDefaultCommonSettings(language)?.LINE_COMMENT_AT_FIRST_COLUMN = true
      }
    }

  }

}

