/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.util.IncorrectOperationException
import com.wuhao.code.check.ancestorOfType
import com.wuhao.code.check.constants.InspectionNames.JAVA_PROPERTY_CLASS
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.inspection.fix.java.SyncDatabaseComment
import com.wuhao.code.check.inspection.fix.vue.ConvertToClassComponent
import com.wuhao.code.check.inspection.visitor.CommonCodeFormatVisitor.Companion.ALL
import com.wuhao.code.check.inspection.visitor.JavaCommentVisitor
import org.jetbrains.kotlin.psi.KtObjectDeclaration

/**
 * Created by 吴昊 on 2017/7/28.
 */
class JavaPropertyClassCreateInspection : BaseInspection(JAVA_PROPERTY_CLASS) {

  private val myQuickFix = MyQuickFix()

  companion object {
    private val LOG = Logger.getInstance("#com.intellij.codeInspection.PropertyClassCreateInspection")
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : JavaElementVisitor() {

      override fun visitClass(aClass: PsiClass) {
        if ((aClass.hasAnnotation(JavaCommentVisitor.ENTITY_CLASS)
                || aClass.hasAnnotation(JavaCommentVisitor.TABLE_CLASS)
                || aClass.hasAnnotation(JavaCommentVisitor.SPRING_DOCUMENT_CLASS)) && aClass.containingFile.containingDirectory.findFile("Q${aClass.name}.java") == null) {
          holder.registerProblem(aClass, Messages.SYNC_DATABASE_COMMENT, ProblemHighlightType.INFORMATION,
              SyncDatabaseComment()
          )
        }
      }

    }
  }

  override fun loadDescription(): String? {
    return "为Entity class 生成一个属性名称的Object类"
  }

  /**
   *
   * @author 吴昊
   * @since 1.4.7
   */
  private class MyQuickFix : LocalQuickFix {

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      try {
        val file = descriptor.psiElement as PsiJavaFile
        if (file.classes.size == 1) {
          val cls = file.classes[0]
          val fieldStr = getPropertyFieldsMap(cls)
          val newFile = PsiFileFactory.getInstance(project)
              .createFileFromText("Q${cls.name}.java", JavaLanguage.INSTANCE, """@SuppressWarnings("$ALL")
            |public class Q${cls.name} {
            |$fieldStr
            |}""".trimMargin())
          file.containingDirectory.add(newFile)
        }
      } catch (e: IncorrectOperationException) {
        LOG.error(e)
      }
    }

    fun buildDeclaration(name: String, prefix: String): String {
      return """  public static String ${buildPropertyName(prefix, name)}"""
    }

    fun buildPropertyName(prefix: String, name: String): String {
      return if (prefix.isEmpty()) {
        name
      } else {
        prefix + name.take(1).toUpperCase() + name.substring(1)
      }
    }

    override fun getFamilyName(): String {
      return name
    }

    override fun getName(): String {
      return "创建属性对象类"
    }

    fun getPrefix(prefix: String): String {
      return if (prefix.isEmpty()) {
        ""
      } else {
        "$prefix."
      }
    }

    fun getPropertyFields(cls: PsiClass): List<PsiField> {
      return cls.allFields.filter { field ->
        !field.name.contains("$")
            && !field.text.contains("const ")
            && field.name != "Companion"
            && field.annotations.none { it.qualifiedName == "javax.persistence.Transient" }
            && (field.ancestorOfType<KtObjectDeclaration>() == null
            || !field.ancestorOfType<KtObjectDeclaration>()!!.isCompanion())
      }
    }

    fun getPropertyFieldsMap(prefix: String, cls: PsiClass): String {
      return getPropertyFields(cls).joinToString("\n") {
        val typeClass = PsiTypesUtil.getPsiClass(it.type)
        if (typeClass != null && typeClass.isEnum) {
          """${buildDeclaration(it.name, prefix)} = "${getPrefix(prefix)}${it.name}";"""
        } else if (typeClass != null && !typeClass.qualifiedName!!.startsWith("java.")) {
          """${buildDeclaration(it.name, prefix)} = "${getPrefix(prefix)}${it.name}";
              ${getPropertyFieldsMap(buildPropertyName(prefix, it.name), typeClass)}"""
        } else {
          """${buildDeclaration(it.name, prefix)} = "${getPrefix(prefix)}${it.name}"; """
        }
      }
    }

    fun getPropertyFieldsMap(cls: PsiClass): String {
      return getPropertyFieldsMap("", cls)
    }

  }

}
