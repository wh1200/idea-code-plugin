/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.processors

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.wuhao.code.check.body
import com.wuhao.code.check.insertAfter
import com.wuhao.code.check.insertBefore
import com.wuhao.code.check.insertElementBefore
import com.wuhao.code.check.style.arrangement.kotlin.KotlinRecursiveVisitor
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset

/**
 * 修复kt文件中的空白行
 * @author 吴昊
 * @since 1.1.2
 */
class FixKotlinBlankLineProcessor : PostFormatProcessor {

  override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
    return source
  }

  override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
    if (source.language is KotlinLanguage) {
      val factory = KtPsiFactory(source.project)
      source.accept(object : KotlinRecursiveVisitor() {

        override fun visitClassBody(classBody: KtClassBody, data: Any?) {
          val lBrace = classBody.lBrace
          val rBrace = classBody.rBrace
          if (lBrace != null && rBrace != null) {
            if (rBrace.prevSibling !is PsiWhiteSpace) {
              rBrace.insertElementBefore(factory.createNewLine(
                  when (lBrace) {
                    rBrace.prevSibling -> 1
                    else -> 2
                  })
              )
            } else {
              if (rBrace.prevSibling === lBrace.nextSibling && rBrace.prevSibling.getLineCount() != 1) {
                rBrace.prevSibling.replace(factory.createNewLine(1))
              } else if (classBody.rBrace!!.prevSibling.getLineCount() != 2) {
                rBrace.prevSibling.replace(factory.createNewLine(2))
              } else if (classBody.lBrace!!.nextSibling.getLineCount() != 2) {
                lBrace.nextSibling.replace(factory.createNewLine(2))
              }
            }
          }
          super.visitClassBody(classBody, data)
        }

        override fun visitDoc(doc: KDoc) {
          //去掉注释与被注释代码之间的空行
          if (doc.nextSibling !is PsiWhiteSpace) {
            doc.insertAfter(factory.createNewLine())
          } else if (doc.nextSibling.getLineCount() != 1) {
            doc.nextSibling.replace(factory.createNewLine())
          }
        }

        override fun visitIfExpression(expression: KtIfExpression, data: Any?) {
          //给if和else if以及else后的代码块添加大括号
          val thens = expression.children.filter { it is KtContainerNodeForControlStructureBody }
          thens.forEach { then ->
            if (then.firstChild !is KtBlockExpression && then.firstChild !is KtIfExpression) {
              if (then.prevSibling is PsiWhiteSpace) {
                then.prevSibling.replace(factory.createWhiteSpace(" "))
              }
              if (then.nextSibling is PsiWhiteSpace) {
                then.nextSibling.replace(factory.createWhiteSpace(" "))
              }
              val block = factory.createBlock(then.text)
              then.replace(block)
            }
          }
          super.visitIfExpression(expression, data)
        }

        override fun visitNamedFunction(function: KtNamedFunction, data: Any?) {
          val body = function.body
          if (body != null) {
            // 方法开头和结束不能留有空行
            val lBrace = body.lBrace
            val rBrace = body.rBrace
            if (lBrace != null) {
              if (lBrace.nextSibling !is PsiWhiteSpace) {
                lBrace.insertAfter(factory.createNewLine())
              } else if (lBrace.nextSibling.getLineCount() != 1) {
                lBrace.nextSibling.replace(factory.createNewLine())
              }
            }
            if (rBrace != null) {
              if (rBrace.prevSibling !is PsiWhiteSpace) {
                rBrace.insertBefore(factory.createNewLine())
              } else if (rBrace.prevSibling.getLineCount() != 1) {
                rBrace.prevSibling.replace(factory.createNewLine())
              }
            }
          }
          super.visitNamedFunction(function, data)
        }

        override fun visitPackageDirective(directive: KtPackageDirective, data: Any?) {
          val elementBeforePackageDirective = directive.prevSibling
          //版权声明与包声明之间不允许有空行
          if (elementBeforePackageDirective != null
              && elementBeforePackageDirective is PsiWhiteSpace
              && elementBeforePackageDirective.getLineCount() != 1) {
            elementBeforePackageDirective.replace(factory.createNewLine())
          }
          val elementAfterDirective = directive.nextSibling
          if (elementAfterDirective is PsiWhiteSpace && elementAfterDirective.getLineCount() != 2) {
            elementAfterDirective.replace(factory.createNewLine(2))
          }
          super.visitPackageDirective(directive, data)
        }
      })
    }
    return TextRange(0, source.endOffset)
  }

}

