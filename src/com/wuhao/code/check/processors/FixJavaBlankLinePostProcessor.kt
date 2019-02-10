/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.processors

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.search.searches.SuperMethodsSearch
import com.wuhao.code.check.*
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.getNextSiblingIgnoringWhitespace
import org.jetbrains.kotlin.psi.psiUtil.getPrevSiblingIgnoringWhitespaceAndComments

private val PsiMethod.isOverrideMethod: Boolean
  get() {
    return SuperMethodsSearch.search(this, null, true, false)
        .findFirst() != null
  }

/**
 * 修复java文件中的空白行
 * @author 吴昊
 * @since 1.1.2
 */
class FixJavaBlankLinePostProcessor : PostFormatProcessor {

  companion object {
    private const val OVERRIDE_DECLARATION = "java.lang.Override"
  }

  override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
    return source
  }

  override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
    if (isIdea) {
      val ktFactory = source.ktPsiFactory
      source.accept(JavaVisitor())
      fixWhiteSpace(source.lastChild, 1, ktFactory)
    }
    return TextRange(0, source.endOffset)
  }

  private fun fixWhiteSpace(whiteSpaceElement: PsiElement?, blankLineCount: Int, factory: KtPsiFactory) {
    if (whiteSpaceElement != null) {
      if (whiteSpaceElement is PsiWhiteSpace) {
        whiteSpaceElement.replace(factory.createWhiteSpace("\n".repeat(blankLineCount)))
      } else {
        whiteSpaceElement.insertElementAfter(factory.createWhiteSpace("\n".repeat(blankLineCount)))
      }
    }
  }

  /**
   *
   * @author 吴昊
   * @since 1.0
   */
  private class JavaVisitor : JavaRecursiveElementVisitor() {

    override fun visitClass(aClass: PsiClass) {
      if (aClass !is PsiTypeParameter
          && aClass !is PsiAnonymousClass) {
        aClass.setBlankLineBoth(1)
      }
      super.visitClass(aClass)
    }

    override fun visitDocComment(comment: PsiDocComment) {
      comment.setBlankLineAfter()
      super.visitDocComment(comment)
    }

    override fun visitElement(element: PsiElement) {
      super.visitElement(element)
    }

    override fun visitField(field: PsiField) {
      if (field !is PsiEnumConstant) {
        val nextField = field.getNextSiblingIgnoringWhitespace()
        val prevField = field.getPrevSiblingIgnoringWhitespaceAndComments()
        if (prevField !is PsiField) {
          field.setBlankLineBefore(1)
        }
        // 两个field之间不留空行
        if (nextField !is PsiField) {
          field.setBlankLineAfter(1)
        } else {
          field.setBlankLineAfter()
        }
      }
      super.visitField(field)
    }

    override fun visitImportList(list: PsiImportList) {
      list.setBlankLineBoth(1)
      super.visitImportList(list)
    }

    override fun visitJavaFile(file: PsiJavaFile) {
      if (file.firstChild is PsiWhiteSpace) {
        file.firstChild.delete()
      }
      super.visitJavaFile(file)
    }

    override fun visitMethod(method: PsiMethod) {
      // 为重载的方法添加@Override注解
      if (method.isOverrideMethod && !method.annotations.any { it.qualifiedName == OVERRIDE_DECLARATION }) {
        val modifierList = method.modifierList
        val annotation = method.psiElementFactory.createAnnotationFromText("@Override", modifierList)
        if (modifierList.children.isEmpty()) {
          modifierList.add(annotation)
        } else {
          val newAnnotation = modifierList.firstChild.insertElementBefore(annotation)
          if (modifierList.getChildrenOfType<PsiAnnotation>().isNotEmpty()) {
            newAnnotation.setBlankLineBefore()
          }
        }
      }
      val nextMethod = method.getNextSiblingIgnoringWhitespace()
      val prevMethod = method.getPrevSiblingIgnoringWhitespaceAndComments()
      if (prevMethod !is PsiMethod) {
        method.setBlankLineBefore(1)
      }
      if (nextMethod is PsiMethod) {
        // 两个方法的修饰符相同时中间保留1个空行，否则保留2个空行
        if (method.modifierList.isEquivalentTo(nextMethod.modifierList)) {
          method.setBlankLineAfter(1)
        } else {
          method.setBlankLineAfter(1)
        }
      } else {
        method.setBlankLineAfter(1)
      }
      super.visitMethod(method)
    }

    override fun visitPackageStatement(statement: PsiPackageStatement) {
      statement.setBlankLineBefore()
      statement.setBlankLineAfter(1)
      super.visitPackageStatement(statement)
    }

  }

}
