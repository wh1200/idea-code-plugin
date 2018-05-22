/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check

import com.intellij.application.options.CodeStyle
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.actions.LastRunReformatCodeOptionsProvider
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.daemon.impl.SeverityRegistrar
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.Language
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.profile.codeInspection.InspectionProfileManager
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
import com.wuhao.code.check.constants.DEFAULT_CONTINUATION_INDENT_SPACE_COUNT
import com.wuhao.code.check.constants.DEFAULT_INDENT_SPACE_COUNT
import com.wuhao.code.check.constants.InspectionNames
import com.wuhao.code.check.style.KotlinModifier.LATEINIT
import com.wuhao.code.check.style.KotlinModifier.OPEN
import com.wuhao.code.check.style.arrangement.*
import com.wuhao.code.check.template.KotlinTemplates
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.plugins.less.LESSFileType
import org.jetbrains.plugins.less.LESSLanguage
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.VueLanguage
import java.awt.Color

/**
 * 项目启动时运行，主要对代码格式的配置按公司规范进行重写
 * @author 吴昊
 * @since 1.2.6
 */
class PluginStart : StartupActivity {

  companion object {
    const val CODE_FORMAT_SEVERITY_NAME: String = "Code Format"

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

  override fun runActivity(project: Project) {
    // 强制启用java代码重排和import重新组织的功能
    val settings = CodeStyle.getSettings(project)
    setRearrange(settings)
    // 设定代码缩进
    setIndent(settings)
    setTemplates(project)
    setSeverity(project)
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

  private fun createLessSettings(): StdArrangementSettings {
    return StdArrangementExtendableSettings(
        listOf(),
        createSections(LessRearrangeRules.get()),
        listOf()
    )
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
    return StdArrangementExtendableSettings(
        listOf(),
        createSections(VueRearrangeRules.get()),
        listOf()
    )
  }

  private fun setIndent(settings: CodeStyleSettings) {
    val setIndentFileTypes = listOf(
        JavaFileType.INSTANCE,
        KotlinFileType.INSTANCE,
        JavaScriptFileType.INSTANCE,
        TypeScriptFileType.INSTANCE,
        LESSFileType.LESS,
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
        is LESSFileType -> LESSLanguage.INSTANCE
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

  private fun setRearrange(settings: CodeStyleSettings) {
    val myLastRunSettings = LastRunReformatCodeOptionsProvider(PropertiesComponent.getInstance())
    myLastRunSettings.saveRearrangeCodeState(true)
    myLastRunSettings.saveOptimizeImportsState(true)
    setLanguageArrangeSettings(myLastRunSettings, settings, JavaLanguage.INSTANCE, createJavaSettings())
    setLanguageArrangeSettings(myLastRunSettings, settings, KotlinLanguage.INSTANCE, createKotlinSettings())
    setLanguageArrangeSettings(myLastRunSettings, settings, VueLanguage.INSTANCE, createVueSettings())
    setLanguageArrangeSettings(myLastRunSettings, settings, LESSLanguage.INSTANCE, createLessSettings())
  }

  private fun setSeverity(project: Project) {
    val severityRegistrar = SeverityRegistrar.getSeverityRegistrar(project)
    val color = Color(255, 227, 96)
    severityRegistrar.registerSeverity(
        SeverityRegistrar.SeverityBasedTextAttributes(
            TextAttributes().apply {
              this.foregroundColor = Color.BLACK
              this.backgroundColor = color
              this.errorStripeColor = color
            },
            HighlightInfoType.HighlightInfoTypeImpl(HighlightSeverity(CODE_FORMAT_SEVERITY_NAME, 350),
                CodeInsightColors.WARNINGS_ATTRIBUTES)
        ), color
    )
    val severity = severityRegistrar.getSeverity(PluginStart.CODE_FORMAT_SEVERITY_NAME)
    val inspectionProfile = InspectionProfileManager.getInstance(project)
        .currentProfile
    InspectionNames.values().forEach {
      inspectionProfile.enableTool(it.shortName, project)
      val tools = inspectionProfile.getTools(it.shortName, project)
      tools.level = HighlightDisplayLevel(severity!!)
    }
  }

  private fun setTemplates(project: Project) {
    val fileTemplateManager = FileTemplateManager.getInstance(project)
    fileTemplateManager.getInternalTemplate("Kotlin File")?.text = KotlinTemplates.FILE
    fileTemplateManager.getInternalTemplate("Kotlin Class")?.text = KotlinTemplates.CLASS
    fileTemplateManager.getInternalTemplate("Kotlin Enum")?.text = KotlinTemplates.ENUM
    fileTemplateManager.getInternalTemplate("Kotlin Interface")?.text = KotlinTemplates.INTERFACE
  }

}

