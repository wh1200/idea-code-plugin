package com.wuhao.code.check.inspection.fix.kotlin

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.util.addAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty

/**
 * 😀 缺少@Api注解的修复
 * @author 吴昊
 * @since 1.4.5
 */
class MissingAnnotationFix(private val fqName: FqName, val exp: String) : LocalQuickFix {

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val el = descriptor.psiElement.parent
    when (el) {
      is KtClass     -> el.addAnnotation(fqName, exp)
      is KtFunction  -> el.addAnnotation(fqName, exp)
      is KtParameter -> el.addAnnotation(fqName, exp)
      is KtProperty  -> el.addAnnotation(fqName, exp)
    }
  }

  override fun getFamilyName(): String {
    return "添加@${fqName.shortName()}注解"
  }

}
