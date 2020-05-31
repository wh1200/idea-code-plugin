package com.wuhao.code.check.inspection.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.constants.InspectionNames.COMPANION_CLASS_TO_OBJECT
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.constants.registerError
import com.wuhao.code.check.ktPsiFactory
import org.jetbrains.kotlin.idea.core.replaced
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtVisitor

/**
 *
 * Created by 吴昊 on 2018/9/17.
 *
 * @author 吴昊
 * @since 1.4.2
 */
class OnlyCompanionClassToObjectInspection : BaseInspection(COMPANION_CLASS_TO_OBJECT) {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : KtVisitor<Any, Any>() {

      override fun visitClass(klass: KtClass, data: Any?) {
        klass.getBody()?.let { classBody ->
          classBody.children.filter {
            it !is PsiWhiteSpace
                && it !is LeafPsiElement
          }.let { it ->
            val onlyBlock = it.firstOrNull()
            if (it.size == 1 && onlyBlock is KtObjectDeclaration && onlyBlock.isCompanion()) {
              klass.nameIdentifier?.let { nameIdentifier ->
                holder.registerError(nameIdentifier, Messages.COMPANION_CLASS_TO_OBJECT, object : LocalQuickFix {

                  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                    val cls = descriptor.psiElement.parent
                    val objectText = if (cls.firstChild is KDoc) {
                      cls.firstChild.text + "\n"
                    } else {
                      ""
                    } + "object ${descriptor.psiElement.text}${onlyBlock.getBody()?.text}"
                    val obj = cls.ktPsiFactory.createObject(objectText)
                    cls.replaced(obj)
                  }

                  override fun getFamilyName(): String {
                    return "将class转换为object"
                  }

                })
              }
            }
          }

        }
      }

    }
  }

}

