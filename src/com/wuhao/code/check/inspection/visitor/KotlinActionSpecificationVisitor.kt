/**
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.wuhao.code.check.constants.Annotations.DELETE_MAPPING
import com.wuhao.code.check.constants.Annotations.FEIGN_CLIENT
import com.wuhao.code.check.constants.Annotations.GET_MAPPING
import com.wuhao.code.check.constants.Annotations.PATH_VARIABLE
import com.wuhao.code.check.constants.Annotations.POST_MAPPING
import com.wuhao.code.check.constants.Annotations.PUT_MAPPING
import com.wuhao.code.check.constants.Annotations.REQUEST_BODY
import com.wuhao.code.check.constants.Annotations.REQUEST_MAPPING
import com.wuhao.code.check.constants.Annotations.REQUEST_PARAM
import com.wuhao.code.check.constants.Annotations.REST_CONTROLLER
import com.wuhao.code.check.constants.Annotations.SWAGGER_API
import com.wuhao.code.check.constants.Annotations.SWAGGER_API_IGNORE
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
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

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
      if (!klass.hasAnnotation(SWAGGER_API) && !klass.hasAnnotation(SWAGGER_API_IGNORE)) {
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
    if (function.containingClassOrObject != null && function.containingClassOrObject!!.hasAnnotation(FEIGN_CLIENT)) {
      if (function.valueParameters.size > 1) {
        function.valueParameters.forEach {
          if (!it.hasAnnotation(PATH_VARIABLE)
              && !it.hasAnnotation(REQUEST_BODY)
              && !it.hasAnnotation(REQUEST_PARAM)) {
            holder.registerError(it.nameIdentifier!!, Messages.MISSING_PARAM_ANNOTATION)
          }
        }
      }
    }

    if (function.hasAnnotation(REQUEST_MAPPING)) {
      holder.registerWarning(
          function.nameIdentifier!!, Messages.REQUEST_MAPPING_ANNOTATION_FORBIDDEN_ON_FUNCTION, DeleteFix()
      )
    }
    val requestAnnotations = listOf(GET_MAPPING, PUT_MAPPING, DELETE_MAPPING, POST_MAPPING).mapNotNull {
      function.getAnnotation(it)
    }
    if (requestAnnotations.isNotEmpty()) {
      if (!function.hasAnnotation(SWAGGER_API_OPERATION)
          && function.containingClassOrObject!!.hasAnnotation(REST_CONTROLLER)
          && !function.containingClassOrObject!!.hasAnnotation(SWAGGER_API_IGNORE)
          && !function.hasAnnotation(SWAGGER_API_IGNORE)) {
        holder.registerWarning(
            function.nameIdentifier!!, Messages.MISSING_API_OPERATION_ANNOTATION,
            MissingAnnotationFix(FqName(SWAGGER_API_OPERATION), "\"\"")
        )
      }
      if (requestAnnotations.size > 1) {
        requestAnnotations.forEach {
          holder.registerWarning(
              it, "GetMapping, PutMapping, PostMapping, DeleteMapping只能使用一个",
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
            when {
              stringEntry.text.startsWith("/") -> holder.registerWarning(
                  argExp, Messages.START_WITH_SLASH_FORBIDDEN,
                  ReplaceWithElementFix {
                    argExp.ktPsiFactory.createStringTemplate(stringEntry.text.substring(1))
                  }
              )
              stringEntry.text.endsWith("/")   -> holder.registerWarning(
                  argExp, Messages.START_WITH_SLASH_FORBIDDEN,
                  ReplaceWithElementFix {
                    argExp.ktPsiFactory.createStringTemplate(stringEntry.text.substring(0, stringEntry.text.length - 1))
                  }
              )
              else                             -> // 路径命名只能使用下划线, 其中需要排除路径参数
                if (stringEntry.text.split("/").any { pathPart ->
                      !pathPart.startsWith("{") && !pathPart.matches(PATH_PATTERN)
                    }) {
                  holder.registerWarning(
                      argExp, "路径命名只能使用小写字母、数字以及下划线（路径参数不受限制），且不能以下划线开头或结尾",
                      ReplaceWithElementFix {
                        argExp.ktPsiFactory.createStringTemplate(
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
