/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassImpl
import com.wuhao.code.check.ancestorOfType
import com.wuhao.code.check.constants.*
import com.wuhao.code.check.enums.NamingMethod
import com.wuhao.code.check.enums.NamingMethod.*
import com.wuhao.code.check.getAncestor
import com.wuhao.code.check.getAncestorsOfType
import com.wuhao.code.check.getLineCount
import com.wuhao.code.check.inspection.fix.DeleteFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Position.Before
import com.wuhao.code.check.inspection.fix.java.JavaConsolePrintFix
import com.wuhao.code.check.inspection.fix.java.JavaElementNameFix
import com.wuhao.code.check.inspection.fix.kotlin.MissingAnnotationFix
import org.jetbrains.kotlin.idea.quickfix.RenameIdentifierFix
import org.jetbrains.kotlin.name.FqName

/**
 * Java代码格式检查访问器
 * Created by 吴昊 on 18-4-26.
 * @author 吴昊
 * @since 1.1
 */
class JavaCodeFormatVisitor(val holder: ProblemsHolder) :
    JavaElementVisitor(), BaseCodeFormatVisitor {

  companion object {
    /**
     * 检查前面是否有空格
     * @param checkElement 被检查的元素
     * @param holder
     * @param position 检查空格的位置
     */
    fun shouldHaveSpaceBeforeOrAfter(checkElement: PsiElement?,
                                     holder: ProblemsHolder,
                                     position: SpaceQuickFix.Position = Before) {
      if (checkElement != null) {
        val fix = SpaceQuickFix(position)
        val place = when (position) {
          Before -> "前面"
          else   -> "后面"
        }
        val check = when (position) {
          Before -> checkElement.prevSibling
          else   -> checkElement.nextSibling
        }
        if (check !is PsiWhiteSpace) {
          holder.registerWarning(checkElement, "${place}应当有空格", fix)
        } else if (check.textLength != 1) {
          holder.registerWarning(checkElement, "${place}应当只有一个空格", fix)
        }
      }
    }
  }

  override fun support(language: Language): Boolean {
    return language == JavaLanguage.INSTANCE
  }

  override fun visitFile(file: PsiFile) {
    if (file.getLineCount() > MAX_LINES_PER_FILE) {
      holder.registerWarning(file, "文件长度不允许超过${MAX_LINES_PER_FILE}行")
    }
  }

  override fun visitForStatement(statement: PsiForStatement) {
    shouldHaveSpaceBeforeOrAfter(statement.condition, holder)
    shouldHaveSpaceBeforeOrAfter(statement.update, holder)
    shouldHaveSpaceBeforeOrAfter(statement.rParenth, holder, SpaceQuickFix.Position.After)
  }

  override fun visitIdentifier(identifier: PsiIdentifier) {
    // 方法名、字段名长度不能少于2个字符, for循环中的变量可以为单字符
    if (identifier.text.length <= 1 && identifier.parent !is PsiTypeParameter
        && (identifier.parent !is PsiLocalVariable || identifier.getAncestor(2) !is PsiDeclarationStatement || identifier.getAncestor(3) !is PsiForStatement) && ((identifier.parent is PsiParameter || identifier.parent is PsiParameterList)
            || (identifier.parent is PsiMethod || identifier.parent is PsiClass)
            || (identifier.parent is PsiField && identifier.getAncestor(2) is PsiClass))) {
      holder.registerWarning(identifier, Messages.NAME_MUST_NOT_LESS_THAN2_CHARS, RenameIdentifierFix())
    }
    val namedElement = identifier.parent
    if (namedElement is PsiClassImpl || namedElement is PsiEnumConstant) {
      identifier.checkNaming(Pascal)
    } else if (namedElement is PsiField || namedElement is PsiLocalVariable
        || (namedElement is PsiMethod && !namedElement.isConstructor)) {
      if (namedElement is PsiField
          && namedElement.hasModifierProperty("static")
          && namedElement.hasModifierProperty("final")) {
        identifier.checkNaming(Constant)
      } else {
        identifier.checkNaming(Camel)
      }
    }
  }

  override fun visitClass(aClass: PsiClass) {
    if ((aClass.hasAnnotation(Annotations.REST_CONTROLLER) || aClass.hasAnnotation(Annotations.CONTROLLER)) && !aClass
            .hasAnnotation(Annotations.REQUEST_MAPPING)) {
      holder.registerError(aClass.nameIdentifier!!, Messages.MISSING_REQUEST_MAPPING_ANNOTATION)
    }
    super.visitClass(aClass)
  }

  override fun visitIfStatement(statement: PsiIfStatement) {
    shouldHaveSpaceBeforeOrAfter(statement.rParenth, holder, SpaceQuickFix.Position.After)
  }

  override fun visitMethod(method: PsiMethod) {
    // 方法长度不能超过指定长度
    if (method.nameIdentifier != null && method.getLineCount() > MAX_LINES_PER_FUNCTION) {
      holder.registerWarning(method.nameIdentifier!!,
          "方法长度不能超过${MAX_LINES_PER_FUNCTION}行")
    }
    if (method.containingClass != null) {
      val restControllerAnno = method.containingClass!!.getAnnotation(Annotations.REST_CONTROLLER)
      val controllerAnno = method.containingClass!!.getAnnotation(Annotations.CONTROLLER)
      if (restControllerAnno != null || controllerAnno != null) {
        val classMappingURI = if (restControllerAnno != null) {
          restControllerAnno.findAttributeValue("value")
        } else {
          controllerAnno!!.findAttributeValue("value")
        }
        val requestAnnotations = listOf(Annotations.GET_MAPPING, Annotations.PUT_MAPPING, Annotations.DELETE_MAPPING, Annotations.POST_MAPPING).mapNotNull {
          method.getAnnotation(it)
        }
        if (requestAnnotations.isNotEmpty()) {
          if (!method.hasAnnotation(Annotations.SWAGGER_API_OPERATION)
              && method.containingClass!!.hasAnnotation(Annotations.REST_CONTROLLER)
              && !method.containingClass!!.hasAnnotation(Annotations.SWAGGER_API_IGNORE)
              && !method.hasAnnotation(Annotations.SWAGGER_API_IGNORE)) {
            holder.registerWarning(
                method.nameIdentifier!!, Messages.MISSING_API_OPERATION_ANNOTATION,
                MissingAnnotationFix(FqName(Annotations.SWAGGER_API_OPERATION), "\"\"")
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
            val methodMappingURI = mappingAnnotation.findAttributeValue("value")
            if ((classMappingURI?.text == null || classMappingURI.text == "/" || classMappingURI.text == "\"\"")
                && methodMappingURI?.text != null && methodMappingURI.text.matches("\"/?\\{.*\\}\"".toRegex())) {
              holder.registerError(mappingAnnotation, Messages.DO_NOT_MATCH_ROOT_PATH)
            }
          }
        }
      }
    }
  }

  override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
    // 使用日志输入代替System.out
    if ((expression.text.startsWith("System.out") || expression.text.startsWith("System.err")) && (expression.ancestorOfType<PsiMethod>() == null
            || !expression.getAncestorsOfType<PsiMethod>().any { func ->
          func.annotations.any { annotation ->
            annotation.qualifiedName == JUNIT_TEST_ANNOTATION_CLASS_NAME
          }
        })) {
      holder.registerWarning(expression, Messages.USE_LOG_INSTEAD_OF_PRINT, JavaConsolePrintFix())
    }
  }

  private fun PsiIdentifier.checkNaming(method: NamingMethod) {
    if (!method.test(this.text)) {
      holder.registerWarning(this, "命名格式错误，格式必须符合${method.zhName}命名法", JavaElementNameFix(method))
    }
  }

}

