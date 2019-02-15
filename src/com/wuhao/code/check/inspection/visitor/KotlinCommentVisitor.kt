/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.*
import com.wuhao.code.check.constants.Messages
import com.wuhao.code.check.constants.Messages.CLASS_COMMENT_REQUIRED
import com.wuhao.code.check.constants.hasDocComment
import com.wuhao.code.check.constants.registerWarning
import com.wuhao.code.check.inspection.fix.DeleteFix
import com.wuhao.code.check.inspection.fix.kotlin.KotlinCommentQuickFix
import com.wuhao.code.check.inspection.visitor.CommonCodeFormatVisitor.Companion.API_MODEL_PROPERTY
import com.wuhao.code.check.ui.PluginSettings
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.core.replaced
import org.jetbrains.kotlin.j2k.getContainingClass
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import org.jetbrains.kotlin.psi.psiUtil.containingClass

/**
 * Created by 吴昊 on 18-4-26.
 */
class KotlinCommentVisitor(val holder: ProblemsHolder) : KtVisitor<Any, Any>(), BaseCodeFormatVisitor {

  override fun support(language: Language): Boolean {
    return language == KotlinLanguage.INSTANCE
  }

  override fun visitClass(klass: KtClass, data: Any?) {
    checkRedundantComment(klass)
    if (klass.hasSuppress(CommonCodeFormatVisitor.ALL)) {
      return
    }
    if (klass !is KtEnumEntry && klass.nameIdentifier != null) {
      if (klass.firstChild == null || klass.firstChild !is KDoc) {
        holder.registerWarning(klass.nameIdentifier!!, CLASS_COMMENT_REQUIRED, KotlinCommentQuickFix())
      }
    }
  }

  override fun visitElement(element: PsiElement) {
    val clazz = element.getContainingClass()
    if (clazz != null && clazz is KtAnnotated && clazz.hasSuppress(CommonCodeFormatVisitor.ALL)) {
      return
    }
    when (element) {
      is KDocSection -> this.visitDocSection(element)
      is KDocTag     -> this.visitDocTag(element)

    }
  }

  override fun visitNamedFunction(function: KtNamedFunction, data: Any?) {
    checkRedundantComment(function)
    if (function.hasSuppress(CommonCodeFormatVisitor.ALL)) {
      return
    }
    // 一等方法必须添加注释
    if (function.isTopLevel && function.firstChild !is KDoc) {
      function.nameIdentifier?.let {
        holder.registerWarning(it, "一等方法必须添加注释", KotlinCommentQuickFix())
      }
    }
    // 接口方法必须添加注释
    if (function.isInterfaceFun() && !function.hasDoc()) {
      holder.registerWarning(function.nameIdentifier ?: function,
          Messages.INTERFACE_METHOD_COMMENT_REQUIRED, KotlinCommentQuickFix())
    }
  }

  override fun visitObjectDeclaration(declaration: KtObjectDeclaration, data: Any?) {
    if (declaration.hasSuppress(CommonCodeFormatVisitor.ALL)) {
      return
    }
    if (!declaration.isCompanion() && declaration.nameIdentifier != null) {
      if (declaration.firstChild == null || declaration.firstChild !is KDoc) {
        holder.registerWarning(declaration.nameIdentifier!!, CLASS_COMMENT_REQUIRED, KotlinCommentQuickFix())
      }
    }
    this.visitElement(declaration)
  }

  override fun visitPackageDirective(directive: KtPackageDirective, data: Any?) {
    checkRedundantComment(directive)
  }

  override fun visitProperty(property: KtProperty, data: Any?) {
    checkRedundantComment(property)
    if (property.hasSuppress(CommonCodeFormatVisitor.ALL)) {
      return
    }
    // 一等属性(非private)必须添加注释
    if (property.isTopLevel && !property.hasDocComment()
        && !property.hasModifier(KtTokens.PRIVATE_KEYWORD)) {
      registerPropertyCommentMissingError(property)
    }
    if (!property.hasAnnotation(API_MODEL_PROPERTY)) {
      // data类字段必须添加注释
      if (property.parent != null && property.parent is KtClassBody
          && property.containingClass() != null
          && property.containingClass()!!.isData()
          && property.firstChild !is KDoc) {
        checkRedundantComment(property)
        registerPropertyCommentMissingError(property)
      }
    }
  }

  private fun checkRedundantComment(element: PsiElement) {
    if (element is KtPackageDirective) {
      val docsBeforeDirective = element.getPrevContinuousSiblingsOfTypeIgnoreWhitespace<KDoc>()
      val commentsBeforeDirective = element.getPrevContinuousSiblingsOfTypeIgnoreWhitespace<PsiComment>()
      if (commentsBeforeDirective.size > 1) {
        registerErrorExceptFirst(commentsBeforeDirective)
      }
      if (docsBeforeDirective.size > 1) {
        registerErrorExceptFirst(docsBeforeDirective)
      }
    } else {
      if (element.firstChild is KDoc) {
        val prev = element.prevIgnoreWs
        if (prev is KDoc) {
          val prevPrev = prev.prevIgnoreWs
          if (prevPrev !is KtImportList && prevPrev !is KtPackageDirective
              || element is KtClass) {
            holder.registerWarning(element.prevIgnoreWs!!, Messages.REDUNDANT_COMMENT, DeleteFix())
          }
        }
      }
    }
  }

  private fun registerErrorExceptFirst(list: List<PsiElement>) {
    list.reversed().drop(1).forEach { comment ->
      holder.registerWarning(comment, Messages.REDUNDANT_COMMENT, DeleteFix())
    }
  }

  private fun registerPropertyCommentMissingError(property: KtProperty) {
    holder.registerWarning(property.nameIdentifier ?: property,
        Messages.COMMENT_REQUIRED, KotlinCommentQuickFix())
  }

  private fun visitDocSection(section: KDocSection) {
    if (!section.text.isBlank() && section.parent.getLineCount() > 0) {
      if (section.prevIgnoreWs is LeafPsiElement
          && (section.prevIgnoreWs as LeafPsiElement).elementType == KDocTokens.START) {
        val textElement = section.allChildren.firstOrNull { it is LeafPsiElement && it.elementType == KDocTokens.TEXT }
        if ((section.firstChild as LeafPsiElement).elementType == KDocTokens.LEADING_ASTERISK
            && section.firstChild.text != "*") {
          holder.registerWarning(section.firstChild, "应该为一个*")
        } else if (textElement == section.firstChild) {
          holder.registerWarning(section.firstChild, "前面应该添加*")
        } else if (textElement == null) {
          holder.registerWarning(section, Messages.MISSING_COMMENT_CONTENT)
        }
      }
    }
  }

  private fun visitDocTag(element: KDocTag) {
    if (element.getContent().isBlank()) {
      when (element.name) {
        "author" -> holder.registerWarning(element, Messages.REQUIRE_AUTHOR, object : LocalQuickFix {

          override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            descriptor.psiElement.replaced(element.ktPsiFactory.createDocTag("author", PluginSettings.INSTANCE.user))
          }

          override fun getFamilyName(): String {
            return "设置当前用户为作者"
          }

        })
        "since"  -> holder.registerWarning(element, Messages.REQUIRE_VERSION, object : LocalQuickFix {

          override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val version = project.getVersion()
            if (version != null) {
              descriptor.psiElement.replaced(element.ktPsiFactory.createDocTag("since", version))
            }
          }

          override fun getFamilyName(): String {
            return "添加当前版本号"
          }

        })
      }
    }
  }

}

