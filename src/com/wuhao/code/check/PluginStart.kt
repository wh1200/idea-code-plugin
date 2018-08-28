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
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.ide.util.PropertiesComponent
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.html.HTMLLanguage
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
import com.intellij.psi.codeStyle.arrangement.match.ArrangementSectionRule
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementEntryMatcher
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementMatchRule
import com.intellij.psi.codeStyle.arrangement.model.ArrangementAtomMatchCondition
import com.intellij.psi.codeStyle.arrangement.model.ArrangementCompositeMatchCondition
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementExtendableSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.BY_NAME
import com.intellij.psi.css.CssFileType
import com.intellij.sql.formatter.settings.SqlCodeStyleSettings
import com.intellij.sql.formatter.settings.SqlCodeStyleSettings.AS_KEYWORDS
import com.intellij.sql.formatter.settings.SqlCodeStyleSettings.TO_LOWER
import com.intellij.sql.formatter.settings.SqlCodeStyleSettings.TO_UPPER
import com.wuhao.code.check.constants.DEFAULT_CONTINUATION_INDENT_SPACE_COUNT
import com.wuhao.code.check.constants.DEFAULT_INDENT_SPACE_COUNT
import com.wuhao.code.check.constants.InspectionNames
import com.wuhao.code.check.style.arrangement.LessRearrangeRules
import com.wuhao.code.check.style.arrangement.RuleDescription
import com.wuhao.code.check.style.arrangement.VueRearrangeRules
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
    setSeverity(project)
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
        JavaScriptFileType.INSTANCE,
        TypeScriptFileType.INSTANCE,
        LESSFileType.LESS,
        JsonFileType.INSTANCE,
        VueFileType.INSTANCE,
        XmlFileType.INSTANCE,
        HtmlFileType.INSTANCE,
        CssFileType.INSTANCE
    )
    setIndentFileTypes.forEach { fileType ->
      val language = when (fileType) {
        is JavaScriptFileType -> JavascriptLanguage.INSTANCE
        is JsonFileType       -> JsonLanguage.INSTANCE
        is VueFileType        -> VueLanguage.INSTANCE
        is HtmlFileType       -> HTMLLanguage.INSTANCE
        is CssFileType        -> CSSLanguage.INSTANCE
        is LESSFileType       -> LESSLanguage.INSTANCE
        else                  -> null
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

}

