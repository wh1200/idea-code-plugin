/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementDecorator
import com.intellij.spring.boot.SpringBootConfigFileConstants.APPLICATION_YML
import com.intellij.spring.boot.application.metadata.SpringBootApplicationMetaConfigKeyManager
import com.intellij.util.ProcessingContext
import com.wuhao.code.check.JAVA_VALUE_ANNOTATION_PATTERN
import com.wuhao.code.check.KOTLIN_VALUE_ANNOTATION_PATTERN
import com.wuhao.code.check.RESOURCES_PATH
import org.jetbrains.kotlin.idea.refactoring.toPsiFile

/**
 * Created by 吴昊 on 2017/7/17.
 */
class SpringBootConfigValueInjectCodeCompletion : CompletionContributor() {

  init {
    val provider = SpringBootConfigPropertiesCompletionProvider()
    extend(CompletionType.BASIC, JAVA_VALUE_ANNOTATION_PATTERN, provider)
    extend(CompletionType.BASIC, KOTLIN_VALUE_ANNOTATION_PATTERN, provider)
  }

  /**
   *
   * @author 吴昊
   * @since 1.2
   */
  inner class SpringBootConfigPropertiesCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(parameters: CompletionParameters,
                                processingContext: ProcessingContext,
                                completionResultSet: CompletionResultSet) {
      val project = parameters.originalFile.project
      val yamlFile = parameters.originalFile.virtualFile.fileSystem
          .findFileByPath("${project.basePath}/$RESOURCES_PATH/$APPLICATION_YML")?.toPsiFile(project)
      if (yamlFile != null) {
        val configKeys = SpringBootApplicationMetaConfigKeyManager.getInstance()
            .getAllMetaConfigKeys(yamlFile)
        configKeys.forEach { configKey ->
          val builder = configKey.presentation.lookupElement
          val insertHandler = LookupElementDecorator.withInsertHandler<LookupElementBuilder>(builder, { _, _ ->
          })
          val lookupElement = configKey.presentation.tuneLookupElement(insertHandler)
          completionResultSet.addElement(lookupElement)
        }
      }
    }
  }
}

