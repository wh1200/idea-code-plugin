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
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.ide.util.PropertiesComponent
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
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
import com.intellij.sql.SqlFileType
import com.intellij.sql.formatter.settings.SqlCodeStyleSettings
import com.intellij.sql.formatter.settings.SqlCodeStyleSettings.*
import com.intellij.sql.psi.SqlLanguage
import com.wuhao.code.check.constants.DEFAULT_CONTINUATION_INDENT_SPACE_COUNT
import com.wuhao.code.check.constants.DEFAULT_INDENT_SPACE_COUNT
import com.wuhao.code.check.constants.InspectionNames
import com.wuhao.code.check.constants.InspectionNames.CODE_FORMAT
import com.wuhao.code.check.constants.InspectionNames.JAVA_COMMENT
import com.wuhao.code.check.constants.InspectionNames.JAVA_FORMAT
import com.wuhao.code.check.constants.InspectionNames.JAVA_PROPERTY_CLASS
import com.wuhao.code.check.constants.InspectionNames.KOTLIN_COMMENT
import com.wuhao.code.check.constants.InspectionNames.KOTLIN_FORMAT
import com.wuhao.code.check.constants.InspectionNames.MYBATIS
import com.wuhao.code.check.constants.InspectionNames.PROPERTY_CLASS
import com.wuhao.code.check.http.HttpRequest
import com.wuhao.code.check.style.KotlinModifier.LATEINIT
import com.wuhao.code.check.style.KotlinModifier.OPEN
import com.wuhao.code.check.style.arrangement.*
import com.wuhao.code.check.template.KotlinTemplates
import com.wuhao.code.check.ui.PluginSettings
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.core.formatter.KotlinCodeStyleSettings
import org.jetbrains.plugins.less.LESSFileType
import org.jetbrains.plugins.less.LESSLanguage
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.YAMLLanguage
import java.awt.Color
import java.util.*

/**
 * 项目启动时运行，主要对代码格式的配置按公司规范进行重写
 * @author 吴昊
 * @since 1.2.6
 */
class PluginStart : StartupActivity {

  var yamlEnabled: Boolean = false

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

  init {
    try {
      Class.forName("org.jetbrains.yaml.YAMLFileType")
      yamlEnabled = true
    } catch (e: Exception) {
      yamlEnabled = false
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
    setDefaults(settings)
    sendEvent(project)
  }

  private fun setKotlinDefaults(settings: CodeStyleSettings) {
    try {
      val kotlinStyleSettings = settings.getCustomSettings(KotlinCodeStyleSettings::class.java)
      kotlinStyleSettings.apply {
        val fields = kotlinStyleSettings.javaClass.fields.map { it.name }
        if (fields.contains("SPACE_BEFORE_WHEN_PARENTHESES")) {
          SPACE_BEFORE_WHEN_PARENTHESES = true
        }
        if (fields.contains("SPACE_AROUND_RANGE")) {
          SPACE_AROUND_RANGE = false
        }
        if (fields.contains("SPACE_BEFORE_EXTEND_COLON")) {
          SPACE_BEFORE_EXTEND_COLON = true
        }
        if (fields.contains("SPACE_AFTER_EXTEND_COLON")) {
          SPACE_AFTER_EXTEND_COLON = true
        }
        if (fields.contains("SPACE_BEFORE_TYPE_COLON")) {
          SPACE_BEFORE_TYPE_COLON = false
        }
        if (fields.contains("SPACE_AFTER_TYPE_COLON")) {
          SPACE_AFTER_TYPE_COLON = true
        }
        if (fields.contains("ALIGN_IN_COLUMNS_CASE_BRANCH")) {
          ALIGN_IN_COLUMNS_CASE_BRANCH = true
        }
        if (fields.contains("SPACE_AROUND_FUNCTION_TYPE_ARROW")) {
          SPACE_AROUND_FUNCTION_TYPE_ARROW = true
        }
        if (fields.contains("SPACE_AROUND_WHEN_ARROW")) {
          SPACE_AROUND_WHEN_ARROW = true
        }
        if (fields.contains("SPACE_BEFORE_LAMBDA_ARROW")) {
          SPACE_BEFORE_LAMBDA_ARROW = true
        }
        if (fields.contains("SPACE_BEFORE_WHEN_PARENTHESES")) {
          SPACE_BEFORE_WHEN_PARENTHESES = true
        }
        if (fields.contains("LBRACE_ON_NEXT_LINE")) {
          LBRACE_ON_NEXT_LINE = false
        }
        if (fields.contains("NAME_COUNT_TO_USE_STAR_IMPORT")) {
          NAME_COUNT_TO_USE_STAR_IMPORT = 5
        }
        if (fields.contains("NAME_COUNT_TO_USE_STAR_IMPORT_FOR_MEMBERS")) {
          NAME_COUNT_TO_USE_STAR_IMPORT_FOR_MEMBERS = 3
        }
        if (fields.contains("CONTINUATION_INDENT_IN_PARAMETER_LISTS")) {
          CONTINUATION_INDENT_IN_PARAMETER_LISTS = true
        }
        if (fields.contains("CONTINUATION_INDENT_IN_ARGUMENT_LISTS")) {
          CONTINUATION_INDENT_IN_ARGUMENT_LISTS = true
        }
        if (fields.contains("CONTINUATION_INDENT_FOR_EXPRESSION_BODIES")) {
          CONTINUATION_INDENT_FOR_EXPRESSION_BODIES = true
        }
        if (fields.contains("CONTINUATION_INDENT_FOR_CHAINED_CALLS")) {
          CONTINUATION_INDENT_FOR_CHAINED_CALLS = true
        }
        if (fields.contains("CONTINUATION_INDENT_IN_SUPERTYPE_LISTS")) {
          CONTINUATION_INDENT_IN_SUPERTYPE_LISTS = true
        }
        if (fields.contains("CONTINUATION_INDENT_IN_IF_CONDITIONS")) {
          CONTINUATION_INDENT_IN_IF_CONDITIONS = true
        }
      }
    } catch (e: Exception) {
    }
  }

  private fun setTypeScriptDefaults(settings: CodeStyleSettings) {
    val typescriptSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings::class.java)

    typescriptSettings.apply {
      val fields = typescriptSettings.javaClass.fields.map { it.name }
      if (fields.contains("JSDOC_INCLUDE_TYPES")) {
        this.JSDOC_INCLUDE_TYPES = true
      }
      if (fields.contains("FUNCTION_EXPRESSION_BRACE_STYLE")) {
        this.FUNCTION_EXPRESSION_BRACE_STYLE = 1
      }
      if (fields.contains("SPACE_BEFORE_FUNCTION_LEFT_PARENTH")) {
        this.SPACE_BEFORE_FUNCTION_LEFT_PARENTH = false
      }
      if (fields.contains("IMPORT_SORT_MEMBERS")) {
        IMPORT_SORT_MEMBERS = true
      }
      if (fields.contains("ENFORCE_TRAILING_COMMA")) {
        ENFORCE_TRAILING_COMMA = JSCodeStyleSettings.TrailingCommaOption.Remove
      }
      if (fields.contains("USE_SEMICOLON_AFTER_STATEMENT")) {
        USE_SEMICOLON_AFTER_STATEMENT = true
      }
      if (fields.contains("FORCE_SEMICOLON_STYLE")) {
        FORCE_SEMICOLON_STYLE = true
      }
      if (fields.contains("FORCE_QUOTE_STYlE")) {
        FORCE_QUOTE_STYlE = true
      }
      if (fields.contains("USE_DOUBLE_QUOTES")) {
        USE_DOUBLE_QUOTES = false
      }
      if (fields.contains("SPACE_BEFORE_FUNCTION_LEFT_PARENTH")) {
        SPACE_BEFORE_FUNCTION_LEFT_PARENTH = false
      }
      if (fields.contains("IMPORT_SORT_MODULE_NAME")) {
        this.IMPORT_SORT_MODULE_NAME = true
      }
      if (fields.contains("IMPORT_MERGE_MEMBERS")) {
        this.IMPORT_MERGE_MEMBERS = JSCodeStyleSettings.BooleanWithGlobalOption.TRUE
      }
      println(fields)
    }
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

  private fun sendEvent(project: Project) {
    var firstFlag = true
    Timer().schedule(object : TimerTask() {

      override fun run() {
        HttpRequest.newPost("http://os.aegis-info.com/api/idea/callback")
            .withParam("email", PluginSettings.INSTANCE.email)
            .withParam("project", project.name)
            .withParam("openedJustNow", firstFlag)
            .withParam("projectVersion", project.getVersion() ?: "")
            .withParam("disposed", project.disposed)
            .withParam("user", PluginSettings.INSTANCE.user.let {
              if (it.isEmpty()) {
                System.getProperty("user.name")
              } else {
                it
              }
            })
            .execute()
        firstFlag = false
      }

    }, 5000, 30_000)
  }

  private fun setDefaults(settings: CodeStyleSettings) {
    if (isIdea) {
      setSqlDefault(settings)
      setKotlinDefaults(settings)
    }
    val jsSettings = settings.getCustomSettings(JSCodeStyleSettings::class.java)
    jsSettings.apply {
      IMPORT_SORT_MEMBERS = true
      ENFORCE_TRAILING_COMMA = JSCodeStyleSettings.TrailingCommaOption.Remove
      USE_SEMICOLON_AFTER_STATEMENT = true
      FORCE_SEMICOLON_STYLE = true
      FORCE_QUOTE_STYlE = true
      USE_DOUBLE_QUOTES = false
      SPACE_BEFORE_FUNCTION_LEFT_PARENTH = false
    }
    setTypeScriptDefaults(settings)
  }

  private fun setIndent(settings: CodeStyleSettings) {
    val setIndentFileTypes = arrayListOf(
        JavaScriptFileType.INSTANCE,
        TypeScriptFileType.INSTANCE,
        LESSFileType.LESS,
        JsonFileType.INSTANCE,
        XmlFileType.INSTANCE,
        HtmlFileType.INSTANCE,
        CssFileType.INSTANCE
    )
    if (vueEnabled) {
      setIndentFileTypes.add(VueFileType.INSTANCE)
    }
    if (isIdea) {
      setIndentFileTypes.addAll(listOf(JavaFileType.INSTANCE,
          SqlFileType.INSTANCE,
          KotlinFileType.INSTANCE))
      if (yamlEnabled) {
        setIndentFileTypes.add(YAMLFileType.YML)
      }
    }
    setIndentFileTypes.forEach { fileType ->
      val language = when (fileType) {
        is JavaScriptFileType -> JavascriptLanguage.INSTANCE
        is JsonFileType       -> JsonLanguage.INSTANCE
        is HtmlFileType       -> HTMLLanguage.INSTANCE
        is CssFileType        -> CSSLanguage.INSTANCE
        is LESSFileType       -> LESSLanguage.INSTANCE
        else                  -> {
          when {
            vueEnabled
                && fileType is VueFileType -> VueLanguage.INSTANCE
            isIdea                         -> if (yamlEnabled && fileType is YAMLFileType) {
              YAMLLanguage.INSTANCE
            } else {
              when (fileType) {
                is KotlinFileType -> KotlinLanguage.INSTANCE
                is JavaFileType   -> JavaLanguage.INSTANCE
                is SqlFileType    -> SqlLanguage.INSTANCE
                else              -> null
              }
            }
            else                           -> null
          }
        }
      }
      if (language != null) {
        setIndent(fileType, language, settings)
      }
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
    if (isIdea) {
      setLanguageArrangeSettings(myLastRunSettings, settings, JavaLanguage.INSTANCE, createJavaSettings())
      setLanguageArrangeSettings(myLastRunSettings, settings, KotlinLanguage.INSTANCE, createKotlinSettings())
    }
    if (vueEnabled) {
      setLanguageArrangeSettings(myLastRunSettings, settings, VueLanguage.INSTANCE, createVueSettings())
    }
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
    val inspectionNames = inspectionProfile.getAllEnabledInspectionTools(project).map { it.shortName }
    InspectionNames.values().forEach {
      if ((isIdea || it !in listOf(CODE_FORMAT, JAVA_COMMENT, JAVA_FORMAT, KOTLIN_COMMENT,
              KOTLIN_FORMAT, PROPERTY_CLASS, JAVA_PROPERTY_CLASS, MYBATIS)) && it.shortName in inspectionNames) {
        inspectionProfile.enableTool(it.shortName, project)
        val tools = inspectionProfile.getTools(it.shortName, project)
        tools.level = HighlightDisplayLevel(severity!!)
      }
    }
  }

  private fun setSqlDefault(settings: CodeStyleSettings) {
    try {
      val sqlStyleSettings = settings.getCustomSettings(SqlCodeStyleSettings::class.java)
      sqlStyleSettings.apply {
        KEYWORD_CASE = TO_UPPER
        TYPE_CASE = AS_KEYWORDS
        IDENTIFIER_CASE = TO_LOWER
      }
    } catch (e: Exception) {
    }
  }

  private fun setTemplates(project: Project) {
    if (isIdea) {
      val fileTemplateManager = FileTemplateManager.getInstance(project)
      fileTemplateManager.apply {
        getInternalTemplate("Kotlin File").text = KotlinTemplates.FILE
        getInternalTemplate("Kotlin Class").text = KotlinTemplates.CLASS
        getInternalTemplate("Kotlin Enum").text = KotlinTemplates.ENUM
        getInternalTemplate("Kotlin Interface").text = KotlinTemplates.INTERFACE
      }
    }
  }

}
