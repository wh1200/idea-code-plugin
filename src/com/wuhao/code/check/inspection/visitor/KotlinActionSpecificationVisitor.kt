/**
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.constants.registerError
import com.wuhao.code.check.getAnnotation
import com.wuhao.code.check.hasAnnotation
import com.wuhao.code.check.inspection.fix.DeleteFix
import com.wuhao.code.check.inspection.fix.ReplaceWithElementFix
import com.wuhao.code.check.inspection.fix.kotlin.MissingAnnotationFix
import com.wuhao.code.check.ktPsiFactory
import com.wuhao.code.check.toUnderlineCase
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.quickfix.RenameIdentifierFix
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*

/**
 * kotlin代码格式检查访问器
 * Created by 吴昊 on 18/4/26.
 *
 * @author 吴昊
 * @since 1.1
 */
class KotlinActionSpecificationVisitor(val holder: ProblemsHolder)
  : KtVisitor<Any, Any>(), BaseCodeFormatVisitor {

  companion object {
    private const val API = "io.swagger.annotations.Api"
    private const val API_OPERATION = "io.swagger.annotations.ApiOperation"
    private const val DELETE_MAPPING: String = "org.springframework.web.bind.annotation.DeleteMapping"
    private const val GET_MAPPING: String = "org.springframework.web.bind.annotation.GetMapping"
    private val PATH_PATTERN = "[a-z0-9]+(_[a-z0-9]+)*".toRegex()
    private const val POST_MAPPING: String = "org.springframework.web.bind.annotation.PostMapping"
    private const val PUT_MAPPING: String = "org.springframework.web.bind.annotation.PutMapping"
    private const val REQUEST_MAPPING: String = "org.springframework.web.bind.annotation.RequestMapping"
    private const val REST_CONTROLLER: String = "org.springframework.web.bind.annotation.RestController"
  }

  override fun support(language: Language): Boolean {
    return language == KotlinLanguage.INSTANCE
  }

  override fun visitClass(klass: KtClass, data: Any?): Any? {
    if (klass.hasAnnotation(REST_CONTROLLER)) {
      if (!klass.hasAnnotation(API)) {
        holder.registerError(
            klass.nameIdentifier!!, Messages.MISSING_API_ANNOTATION,
            MissingAnnotationFix(FqName(API), """tags = [""]""")
        )
      }
      if (!klass.hasAnnotation(REQUEST_MAPPING)) {
        holder.registerError(
            klass.nameIdentifier!!, Messages.MISSING_REQUEST_MAPPING_ANNOTATION,
            MissingAnnotationFix(FqName(REQUEST_MAPPING), """""""")
        )
      } else {
        val requestMappingAnnotation = klass.getAnnotation(REQUEST_MAPPING)
        checkMappingAnnotation(requestMappingAnnotation)
      }
      if (klass.name != null && !klass.name!!.endsWith("Action")) {
        holder.registerError(
            klass.nameIdentifier!!, Messages.NAME_END_WITH_ACTION,
            RenameIdentifierFix()
        )
      }
    }
    return super.visitClass(klass, data)
  }

  override fun visitElement(element: PsiElement?) {
    super.visitElement(element)
  }

  override fun visitNamedFunction(function: KtNamedFunction, data: Any?): Any? {
    if (function.hasAnnotation(REQUEST_MAPPING)) {
      holder.registerError(
          function.nameIdentifier!!, Messages.REQUEST_MAPPING_ANNOTATION_FORBIDDEN_ON_FUNCTION, DeleteFix()
      )
    }
    val requestAnnotations = listOf(GET_MAPPING, PUT_MAPPING, DELETE_MAPPING, POST_MAPPING).mapNotNull {
      function.getAnnotation(it)
    }
    if (requestAnnotations.isNotEmpty()) {
      if (!function.hasAnnotation(API_OPERATION)) {
        holder.registerError(
            function.nameIdentifier!!, Messages.MISSING_API_OPERATION_ANNOTATION,
            MissingAnnotationFix(FqName(API_OPERATION), "\"\"")
        )
      }
      if (requestAnnotations.size > 1) {
        requestAnnotations.forEach {
          holder.registerError(
              it, "GetMapping,PutMapping,PostMapping,DeleteMapping只能使用一个",
              DeleteFix()
          )
        }
      } else {
        val mappingAnnotation = requestAnnotations.first()
        checkMappingAnnotation(mappingAnnotation)
      }
    }
    return super.visitNamedFunction(function, data)
  }

  private fun checkMappingAnnotation(annotation: KtAnnotationEntry?) {
    annotation?.valueArguments?.forEach {
      if ((it.isNamed() && it.getArgumentName()!!.asName.asString() == "value") || !it.isNamed()) {
        val argExp = it.getArgumentExpression()
        if (argExp is KtStringTemplateExpression) {
          argExp.entries.forEach { stringEntry ->
            if (stringEntry.text.startsWith("/")) {
              holder.registerError(
                  stringEntry, Messages.START_WITH_SLASH_FORBIDDEN,
                  ReplaceWithElementFix {
                    stringEntry.ktPsiFactory.createArgument(stringEntry.text.substring(1))
                  }
              )
            } else if (stringEntry.text.endsWith("/")) {
              holder.registerError(
                  stringEntry, Messages.START_WITH_SLASH_FORBIDDEN,
                  ReplaceWithElementFix {
                    stringEntry.ktPsiFactory.createArgument(stringEntry.text.substring(0, stringEntry.text.length - 1))
                  }
              )
            } else {
              // 路径命名只能使用下划线, 其中需要排除路径参数
              if (stringEntry.text.split("/").any {
                    !it.startsWith("{") && !it.matches(PATH_PATTERN)
                  }) {
                holder.registerError(
                    stringEntry, "路径命名只能使用小写字母、数字以及下划线（路径参数不受限制），且不能以下划线开头或结尾",
                    ReplaceWithElementFix {
                      stringEntry.ktPsiFactory.createArgument(
                          stringEntry.text.split("/").map {
                            if (!it.startsWith("{") && !it.matches(PATH_PATTERN)) {
                              it.toUnderlineCase().toLowerCase()
                            } else {
                              it
                            }
                          }.joinToString("/")
                      )
                    }
                )
              }
            }
          }
        }
      }
    }
  }

}
