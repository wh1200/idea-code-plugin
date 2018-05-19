/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.*
import com.intellij.psi.javadoc.PsiDocComment
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.inspection.fix.java.JavaBlockCommentFix
import com.wuhao.code.check.constants.registerError

/**
 * Java代码格式检查访问器
 * Created by 吴昊 on 18-4-26.
 * @author 吴昊
 * @since 1.1
 */
class JavaCommentVisitor(val holder: ProblemsHolder) :
    JavaElementVisitor(), BaseCodeFormatVisitor {

  companion object {
    const val ENTITY_CLASS = "javax.persistence.Entity"
    const val SPRING_DOCUMENT_CLASS = "org.springframework.data.elasticsearch.annotations.Document"
    const val TABLE_CLASS = "javax.persistence.Table"
  }

  override fun support(language: Language): Boolean {
    return language == JavaLanguage.INSTANCE
  }

  /**
   *
   * @param clazz 类元素
   */
  override fun visitClass(clazz: PsiClass) {
    if (clazz !is PsiTypeParameter && (clazz.firstChild == null || clazz.firstChild !is PsiDocComment) && clazz !is PsiAnonymousClass) {
      if (clazz.nameIdentifier != null) {
        holder.registerError(clazz.nameIdentifier!!, Messages.CLASS_COMMENT_REQUIRED, JavaBlockCommentFix())
      } else {
        holder.registerError(clazz, Messages.CLASS_COMMENT_REQUIRED, JavaBlockCommentFix())
      }
    }
    if (clazz.annotations.any { it.qualifiedName in listOf(ENTITY_CLASS, TABLE_CLASS, SPRING_DOCUMENT_CLASS) }) {
      clazz.fields.filter {
        !it.hasModifier(JvmModifier.STATIC) && it.hasModifier(JvmModifier.PRIVATE)
            && it.firstChild !is PsiDocComment
      }.forEach { fieldElement ->
        holder.registerError(fieldElement.nameIdentifier, Messages.COMMENT_REQUIRED,
            JavaBlockCommentFix())
      }
    }
  }

  override fun visitMethod(method: PsiMethod) {
    // 接口方法必须包含注释
    val elClass = method.containingClass
    if (elClass != null && elClass.isInterface && method.firstChild !is PsiDocComment) {
      val elementToRegisterProblem = if (method.nameIdentifier != null) {
        method.nameIdentifier!!
      } else {
        method
      }
      holder.registerError(elementToRegisterProblem,
          Messages.INTERFACE_METHOD_COMMENT_REQUIRED, JavaBlockCommentFix())
    }
  }

}

