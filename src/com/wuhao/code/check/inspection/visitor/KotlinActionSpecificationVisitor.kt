/**
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.wuhao.code.check.constants.Annotations.DELETE_MAPPING
import com.wuhao.code.check.constants.Annotations.GET_MAPPING
import com.wuhao.code.check.constants.Annotations.POST_MAPPING
import com.wuhao.code.check.constants.Annotations.PUT_MAPPING
import com.wuhao.code.check.constants.Annotations.REQUEST_MAPPING
import com.wuhao.code.check.constants.Annotations.REST_CONTROLLER
import com.wuhao.code.check.constants.Annotations.SWAGGER_API
import com.wuhao.code.check.constants.Annotations.SWAGGER_API_OPERATION
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.constants.registerError
import com.wuhao.code.check.constants.registerWarning
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
    val PATH_PATTERN = "[a-z0-9]+(_[a-z0-9]+)*".toRegex()
  }

  override fun support(language: Language): Boolean {
    return language == KotlinLanguage.INSTANCE
  }

  override fun visitClass(klass: KtClass, data: Any?): Any? {
    // 对RestController作规范约束
    if (klass.hasAnnotation(REST_CONTROLLER)) {
      // 必须要有 io.swagger.annotations.Api 注解
      if (!klass.hasAnnotation(SWAGGER_API)) {
        holder.registerWarning(
            klass.nameIdentifier!!, Messages.MISSING_API_ANNOTATION,
            MissingAnnotationFix(FqName(SWAGGER_API), """tags = [""]""")
        )
      }
      // 必须要有 org.springframework.web.bind.annotation.RequestMapping 注解
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
        holder.registerWarning(
            klass.nameIdentifier!!, Messages.NAME_END_WITH_ACTION,
            RenameIdentifierFix()
        )
      }
    }
    return super.visitClass(klass, data)
  }

  override fun visitNamedFunction(function: KtNamedFunction, data: Any?): Any? {
    if (function.hasAnnotation(REQUEST_MAPPING)) {
      holder.registerWarning(
          function.nameIdentifier!!, Messages.REQUEST_MAPPING_ANNOTATION_FORBIDDEN_ON_FUNCTION, DeleteFix()
      )
    }
    val requestAnnotations = listOf(GET_MAPPING, PUT_MAPPING, DELETE_MAPPING, POST_MAPPING).mapNotNull {
      function.getAnnotation(it)
    }
    if (requestAnnotations.isNotEmpty()) {
      if (!function.hasAnnotation(SWAGGER_API_OPERATION)) {
        holder.registerWarning(
            function.nameIdentifier!!, Messages.MISSING_API_OPERATION_ANNOTATION,
            MissingAnnotationFix(FqName(SWAGGER_API_OPERATION), "\"\"")
        )
      }
      if (requestAnnotations.size > 1) {
        requestAnnotations.forEach {
          holder.registerWarning(
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
              holder.registerWarning(
                  stringEntry, Messages.START_WITH_SLASH_FORBIDDEN,
                  ReplaceWithElementFix {
                    stringEntry.ktPsiFactory.createArgument(stringEntry.text.substring(1))
                  }
              )
            } else if (stringEntry.text.endsWith("/")) {
              holder.registerWarning(
                  stringEntry, Messages.START_WITH_SLASH_FORBIDDEN,
                  ReplaceWithElementFix {
                    stringEntry.ktPsiFactory.createArgument(stringEntry.text.substring(0, stringEntry.text.length - 1))
                  }
              )
            } else {
              // 路径命名只能使用下划线, 其中需要排除路径参数
              if (stringEntry.text.split("/").any { pathPart ->
                    !pathPart.startsWith("{") && !pathPart.matches(PATH_PATTERN)
                  }) {
                holder.registerWarning(
                    stringEntry, "路径命名只能使用小写字母、数字以及下划线（路径参数不受限制），且不能以下划线开头或结尾",
                    ReplaceWithElementFix {
                      stringEntry.ktPsiFactory.createArgument(
                          stringEntry.text.split("/").joinToString("/") { pathPart ->
                            if (!pathPart.startsWith("{") && !pathPart.matches(PATH_PATTERN)) {
                              pathPart.toUnderlineCase().toLowerCase()
                            } else {
                              pathPart
                            }
                          }
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
