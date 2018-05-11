/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.processors

import com.intellij.lang.jvm.JvmModifier
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.JavaTokenType.LBRACE
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.wuhao.code.check.getPsiElementFactory
import com.wuhao.code.check.insertAfter
import com.wuhao.code.check.lang.RecursiveVisitor
import org.jetbrains.kotlin.idea.hierarchy.overrides.isOverrideHierarchyElement
import org.jetbrains.kotlin.psi.psiUtil.endOffset

/**
 * 修复java类中的元素顺序
 * @author 吴昊
 * @since 1.1.2
 */
class FixJavaElementOrder : PostFormatProcessor {

  fun PsiMethod.compare(method: PsiMethod): Int {
    return if (this.modifiers.contentEquals(method.modifiers)) {
      this.compareOverride(method)
    } else {
      val modifierCompare = method.modifiers.compareTo(this.modifiers)
      if (modifierCompare == 0) {
        this.compareOverride(method)
      } else {
        modifierCompare
      }
    }
  }

  override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
    return source
  }

  override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
    object : RecursiveVisitor() {

      override fun visitElement(element: PsiElement) {
        if (element is PsiClass) {
          fixElementOrder(element)
        }
      }

    }.visit(source)
    return TextRange(0, source.endOffset)
  }

  private fun PsiMethod.compareOverride(method: PsiMethod): Int {
    return if (this.isOverrideHierarchyElement() && method.isOverrideHierarchyElement()) {
      this.name.compareTo(method.name)
    } else if (this.isOverrideHierarchyElement()) {
      1
    } else if (method.isOverrideHierarchyElement()) {
      -1
    } else {
      this.name.compareTo(method.name)
    }
  }

  /**
   * 按修饰符排序
   * @param modifiers
   * @return
   */
  private fun Array<JvmModifier>.compareTo(modifiers: Array<JvmModifier>): Int {
    return this.getWeight().compareTo(modifiers.getWeight())
  }

  private fun PsiField.compareTo(field: PsiField): Int {
    return if (this.modifiers.contentEquals(field.modifiers)) {
      this.name.compareTo(field.name)
    } else {
      val modifierCompare = field.modifiers.compareTo(this.modifiers)
      if (modifierCompare == 0) {
        this.name.compareTo(field.name)
      } else {
        modifierCompare
      }
    }
  }

  private fun fixElementOrder(psiClass: PsiClass) {
    val leftBrace = psiClass.children.firstOrNull {
      it is PsiJavaToken && it.tokenType == LBRACE
    }
    val factory = getPsiElementFactory(psiClass)
    if (leftBrace != null) {
      val fields = psiClass.fields.sortedWith(Comparator { field1, field2 ->
        field1.compareTo(field2)
      })
      val methods = psiClass.methods.sortedWith(Comparator { m1, m2 ->
        m1.compare(m2)
      })
      fields.forEach { it.delete() }
      methods.forEach { it.delete() }
      val newFields = fields.map { factory.createFieldFromText(it.text, null) }
      var relativeElement: PsiElement = leftBrace
      newFields.forEach { field ->
        relativeElement = field.insertAfter(relativeElement)
      }
      val newMethods = methods.map { factory.createMethodFromText(it.text, null) }
      newMethods.forEach { method ->
        relativeElement = method.insertAfter(relativeElement)
      }
    }
  }

  private fun Array<JvmModifier>.getWeight(): Int {
    return this.map { it.getWeight() }.sum()
  }

  /**
   * 获取java修饰符的权重
   * @return 权重值
   */
  private fun JvmModifier.getWeight(): Int {
    val weight = when (this) {
      JvmModifier.PUBLIC -> 9
      JvmModifier.PROTECTED -> 6
      JvmModifier.PRIVATE -> 4
      JvmModifier.PACKAGE_LOCAL -> 5
      JvmModifier.STATIC -> 8
      JvmModifier.ABSTRACT -> 10
      JvmModifier.FINAL -> 7
      JvmModifier.NATIVE -> 1
      JvmModifier.SYNCHRONIZED -> 1
      JvmModifier.STRICTFP -> 1
      JvmModifier.TRANSIENT -> 1
      JvmModifier.VOLATILE -> 1
      JvmModifier.TRANSITIVE -> 1
    }
    return Math.pow(2.0, weight.toDouble()).toInt()
  }

}

