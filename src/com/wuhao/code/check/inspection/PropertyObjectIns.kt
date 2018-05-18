/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection

import com.intellij.codeInsight.daemon.GroupNames
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.util.IncorrectOperationException
import com.wuhao.code.check.ancestorOfType
import com.wuhao.code.check.ktPsiFactory
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration

/**
 * Created by 吴昊 on 2017/7/28.
 */
class PropertyClassCreateInspection : LocalInspectionTool() {

  private val myQuickFix = MyQuickFix()

  companion object {
    private val LOG = Logger.getInstance("#com.intellij.codeInspection.PropertyClassCreateInspection")
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : PsiElementVisitor() {

      override fun visitFile(el: PsiFile) {
        if (el.language is KotlinLanguage && el is KtFile && el.classes.size == 1) {
          if (el.classes[0].annotations.any {
                it.qualifiedName in listOf("javax.persistence.Entity",
                    "org.springframework.data.elasticsearch.annotations.Document")
              }) {
            holder.registerProblem(el, "create property class", myQuickFix)
          }
        }
      }

    }
  }

  override fun getDisplayName(): String {
    return "create property class"
  }

  override fun getGroupDisplayName(): String {
    return GroupNames.BUGS_GROUP_NAME
  }

  override fun getShortName(): String {
    return "EntityPropertyClassCreation"
  }

  override fun isEnabledByDefault(): Boolean {
    return true
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
          val newCls = factory.createObject("""object Q${cls.name} {
            $fieldStr
          }""")
          val field = factory.createProperty("q${cls.name?.take(1)?.toLowerCase() + cls.name?.substring(1)}", null, false, "Q${cls.name}")
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
      return "create property object class"
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
          }}${it
              .name}""""
        } else if (typeClass != null && !typeClass.qualifiedName!!.startsWith("java.")) {
          """const val ${buildPropertyName(prefix, it.name)} = "${if (prefix.isEmpty()) {
            ""
          } else {
            "$prefix."
          }}${it
              .name}"
              ${getPropertyFieldsMap(buildPropertyName(prefix, it.name), typeClass)}"""
        } else {
          """const val ${buildPropertyName(prefix, it.name)} = "${if (prefix.isEmpty()) {
            ""
          } else {
            "$prefix."
          }}${it
              .name}" """
        }
      }
    }

    fun getPropertyFieldsMap(cls: PsiClass): String {
      return getPropertyFieldsMap("", cls)
    }

  }

}

