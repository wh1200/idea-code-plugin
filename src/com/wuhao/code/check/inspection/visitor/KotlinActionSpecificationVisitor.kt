/**
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.constants.registerError
import com.wuhao.code.check.hasAnnotation
import com.wuhao.code.check.inspection.fix.kotlin.MissingAnnotationFix
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.quickfix.RenameIdentifierFix
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtVisitor

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
    private const val POST_MAPPING: String = "org.springframework.web.bind.annotation.PostMapping"
    private const val PUT_MAPPING: String = "org.springframework.web.bind.annotation.PutMapping"
    private const val REQUEST_MAPPING: String = "org.springframework.web.bind.annotation.RequestMapping"
    private const val REST_CONTROLLER: String = "org.springframework.web.bind.annotation.RestController"
  }

  override fun support(language: Language): Boolean {
    return language == KotlinLanguage.INSTANCE
  }

  override fun visitElement(element: PsiElement?) {
    super.visitElement(element)
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

  override fun visitNamedFunction(function: KtNamedFunction, data: Any?): Any? {
    if (function.hasAnnotation(REQUEST_MAPPING)) {
      holder.registerError(
          function.nameIdentifier!!, Messages.REQUEST_MAPPING_ANNOTATION_FORBIDDEN_ON_FUNCTION
      )
    }
    if (listOf(GET_MAPPING, PUT_MAPPING, DELETE_MAPPING, POST_MAPPING).any { function.hasAnnotation(it) }
        && !function.hasAnnotation(API_OPERATION)) {
      holder.registerError(
          function.nameIdentifier!!, Messages.MISSING_API_OPERATION_ANNOTATION,
          MissingAnnotationFix(FqName(API_OPERATION), "\"\"")
      )
    }
    return super.visitNamedFunction(function, data)
  }

}
