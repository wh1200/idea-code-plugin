/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.processors

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.*
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Position.After
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Position.Before
import com.wuhao.code.check.inspection.fix.kotlin.buildComment
import com.wuhao.code.check.style.arrangement.kotlin.KotlinRecursiveVisitor
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

/**
 * 修复kt文件中的空白行
 * @author 吴昊
 * @since 1.1.2
 */
class FixKotlinPostProcessor : PostFormatProcessor {

  override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
    return source
  }

  override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
    if (source.language is KotlinLanguage) {
      source.accept(KotlinFixVisitor(source.ktPsiFactory))
    }
    return TextRange(0, source.endOffset)
  }

}

/**
 * kotlin代码修正递归访问器
 * @author 吴昊
 * @since 1.3.3
 */
class KotlinFixVisitor(private val factory: KtPsiFactory) : KotlinRecursiveVisitor() {

  companion object {
    private const val BLANK_LINES_BETWEEN_FUNCTIONS = 2
  }

  override fun visitClass(klass: KtClass, data: Any?) {
    if (klass.isEnum()) {
      val factory = klass.ktPsiFactory
      klass.getBody()?.let { body ->
        val oldEntries = body.getChildrenOfType<KtEnumEntry>()
        if (oldEntries.isNotEmpty()) {
          val commentMap = oldEntries.associateBy({ it }, {
            if (it.lastChild is PsiComment) {
              it.lastChild as PsiComment
            } else {
              null
            }
          })
          commentMap.values.forEach { it?.delete() }
          val commentStringMap = hashMapOf<String, String>()
          oldEntries.forEach { entry -> entry.delete() }
          val texts = oldEntries
              .sortedByDescending { it.name }
              .map { entry ->
                val comment = commentMap[entry]
                val commentText = comment?.text ?: ""
                var text = if (entry.text.endsWith(";")) {
                  entry.text.dropLast(1)
                } else {
                  entry.text.trim()
                }
                if (!text.endsWith(",")) {
                  text += ","
                }
                commentStringMap[text] = commentText
                text
              }
          val maxLength = texts.map { it.length }.max()!!
          val entries = texts.mapIndexed { index, it ->
            var text = it
            val comment = commentStringMap[text]!!
            if (index == 0) {
              text = text.dropLast(1) + ";"
            }
            val newEntry = factory.createEnumEntry(text)
            if (comment.isNotBlank()) {
              newEntry.add(factory.createWhiteSpace(" ".repeat(maxLength - text.length + 2)))
              newEntry.add(factory.createComment(comment))
            }
            newEntry
          }
          if (body.lBrace!!.nextSibling is PsiWhiteSpace) {
            entries.forEach { it.insertAfter(body.lBrace!!.nextSibling) }
          } else {
            entries.forEach { it.insertAfter(body.lBrace!!) }
          }
          val newEntries = body.getChildrenOfType<KtEnumEntry>()
          newEntries.forEach { newEntry ->
            if (newEntry.nextSibling is KtEnumEntry) {
              newEntry.setBlankLineAfter()
            }
          }
        }
      }
    }
    super.visitClass(klass, data)
  }

  override fun visitClassBody(classBody: KtClassBody, data: Any?) {
    val parent = classBody.parent
    val lBrace = classBody.lBrace
    val rBrace = classBody.rBrace
    if (lBrace != null && rBrace != null) {
      if (parent is KtObjectDeclaration && parent.isCompanion()) { // 伴随对象body前后不留空行
        lBrace.clearBlankLineBeforeOrAfter(After)
        rBrace.clearBlankLineBeforeOrAfter(Before)
      } else {
        when {
          lBrace.next == rBrace -> lBrace.setBlankLineAfter()
          rBrace.prev !is PsiWhiteSpace -> lBrace.setBlankLineAfter(1)
          rBrace.prevIgnoreWs === lBrace -> // 如果classBody没有内容的话，右括号保持换行，左右括号之间不留空行
            rBrace.setBlankLineBefore()
          else -> {
            // 如果classBody有内容，则左括号后和右括号前各留一个空行
            lBrace.setBlankLineAfter(1)
            rBrace.setBlankLineBefore(1)
          }
        }
      }
    }
    super.visitClassBody(classBody, data)
  }

  override fun visitDoc(doc: KDoc) {
    //去掉注释与被注释代码之间的空行
    doc.setBlankLineAfter()
  }

  override fun visitEnumEntry(enumEntry: KtEnumEntry, data: Any?) {
    // 删除最后一个枚举元素后面的逗号
    if (enumEntry == enumEntry.parent.getChildrenOfType<KtEnumEntry>().last()) {
      val lastEntryComma = enumEntry.lastChild
      if (lastEntryComma is LeafPsiElement && lastEntryComma.elementType == KtTokens.COMMA) {
        lastEntryComma.delete()
      }
    }
    super.visitEnumEntry(enumEntry, data)
  }

  override fun visitIfExpression(expression: KtIfExpression, data: Any?) {
    //给if和else if以及else后的代码块添加大括号
    val thens = expression.children.filter { it is KtContainerNodeForControlStructureBody }
    thens.forEach { then ->
      if (then.firstChild !is KtBlockExpression && then.firstChild !is KtIfExpression) {
        if (then.prev is PsiWhiteSpace) {
          then.prev.replace(factory.createWhiteSpace(" "))
        }
        if (then.next is PsiWhiteSpace) {
          then.next.replace(factory.createWhiteSpace(" "))
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
      body.lBrace?.clearBlankLineBeforeOrAfter(After)
      body.rBrace?.clearBlankLineBeforeOrAfter(Before)
    }
    if (function.nextIgnoreWs is KtNamedFunction) {
      function.setBlankLineAfter(BLANK_LINES_BETWEEN_FUNCTIONS)
    }
    if (function.isInterfaceFun() && !function.hasDoc()) {
      function.addBefore(buildComment(function), function.firstChild)
    }
    super.visitNamedFunction(function, data)
  }

  override fun visitPackageDirective(directive: KtPackageDirective, data: Any?) {
    //版权声明与包声明之间不允许有空行
    if (directive.prevIgnoreWs is PsiComment || directive.prevIgnoreWs is KDoc) {
      directive.setBlankLineBefore()
    }
    directive.setBlankLineAfter(1)
    super.visitPackageDirective(directive, data)
  }

}

