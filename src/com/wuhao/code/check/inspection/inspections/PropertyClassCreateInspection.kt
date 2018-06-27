/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiField
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.util.IncorrectOperationException
import com.wuhao.code.check.*
import com.wuhao.code.check.constants.InspectionNames.PROPERTY_CLASS
import com.wuhao.code.check.inspection.visitor.JavaCommentVisitor.Companion.ENTITY_CLASS
import com.wuhao.code.check.inspection.visitor.JavaCommentVisitor.Companion.SPRING_DOCUMENT_CLASS
import com.wuhao.code.check.inspection.visitor.JavaCommentVisitor.Companion.TABLE_CLASS
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtVisitor

/**
 * Created by 吴昊 on 2017/7/28.
 */
class PropertyClassCreateInspection : BaseInspection(PROPERTY_CLASS) {

  private val myQuickFix = MyQuickFix()

  companion object {
    private val LOG = Logger.getInstance("#com.intellij.codeInspection.PropertyClassCreateInspection")
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : KtVisitor<Any, Any>() {

      override fun visitClass(klass: KtClass, data: Any?) {
        if (klass.hasAnnotation(ENTITY_CLASS) || klass.hasAnnotation(TABLE_CLASS)
            || klass.hasAnnotation(SPRING_DOCUMENT_CLASS)) {
          val file = klass.containingKtFile
          if (!file.findObjectClass("Q${klass.name}")) {
            holder.registerProblem(klass.containingKtFile, "创建属性名称对象", myQuickFix)
          }
        }
      }

    }
  }


  override fun loadDescription(): String? {
    return "为Entity class 生成一个属性名称的Object类"
  }

  /**
   *
   * @author
   * @since
   */
  private class MyQuickFix : LocalQuickFix {

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      try {
        val file = descriptor.psiElement as KtFile
        if (file.classes.size == 1) {
          val cls = file.classes[0]
          val factory = file.ktPsiFactory
          val fieldStr = getPropertyFieldsMap(cls)
          val newCls = factory.createObject("""@Suppress("warnings")
            object Q${cls.name} {
            $fieldStr
          }""")
          val field = factory.createProperty("""q${cls.name?.take(1)?.toLowerCase() + cls.name?.substring(1)}""".trimMargin(), null, false, "Q${cls.name}")
          field.firstChild.insertElementBefore(factory.createModifierList("""@Suppress("warnings")"""))
          file.add(newCls)
          file.add(field)
        }
      } catch (e: IncorrectOperationException) {
        LOG.error(e)
      }
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


    fun getPropertyFields(cls: PsiClass): List<PsiField> {
      return cls.allFields.filter {
        !it.name.contains("$")
            && !it.text.contains("const ")
            && it.name != "Companion"
            && it.annotations.none { it.qualifiedName == "javax.persistence.Transient" }
            && (it.ancestorOfType<KtObjectDeclaration>() == null
            || !it.ancestorOfType<KtObjectDeclaration>()!!.isCompanion())
      }
    }


    fun getPropertyFieldsMap(prefix: String, cls: PsiClass): String {
      return getPropertyFields(cls).joinToString("\n") {
        val typeClass = PsiTypesUtil.getPsiClass(it.type)
        if (typeClass != null && typeClass.isEnum) {
          """const val ${buildPropertyName(prefix, it.name)} =  "${if (prefix.isEmpty()) {
            ""
          } else {
            "$prefix."
          }}${it.name}""""
        } else if (typeClass != null && !typeClass.qualifiedName!!.startsWith("java.")) {
          """const val ${buildPropertyName(prefix, it.name)} = "${if (prefix.isEmpty()) {
            ""
          } else {
            "$prefix."
          }}${it.name}"
              ${getPropertyFieldsMap(buildPropertyName(prefix, it.name), typeClass)}"""
        } else {
          """const val ${buildPropertyName(prefix, it.name)} = "${if (prefix.isEmpty()) {
            ""
          } else {
            "$prefix."
          }}${it.name}" """
        }
      }
    }


    fun getPropertyFieldsMap(cls: PsiClass): String {
      return getPropertyFieldsMap("", cls)
    }

  }

}

