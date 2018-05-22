package com.wuhao.code.check.processors

import com.intellij.lang.ASTNode
import com.intellij.lang.css.CSSLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.css.CssDeclaration
import com.intellij.psi.css.CssElement
import com.intellij.psi.css.CssRuleset
import com.intellij.psi.css.CssRulesetList
import com.intellij.psi.css.impl.CssElementTypes
import com.intellij.psi.css.impl.CssElementTypes.CSS_RBRACE
import com.intellij.psi.css.impl.CssTokenImpl
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.wuhao.code.check.*
import com.wuhao.code.check.style.arrangement.less.LessRecursiveVisitor
import org.jetbrains.plugins.less.LESSLanguage

/**
 * Created by 吴昊 on 2018/5/22.
 * @author 吴昊
 * @since 1.4
 */
class FixLessPreProcessor : PreFormatProcessor {

  override fun process(astNode: ASTNode, textRange: TextRange): TextRange {
    if (astNode.psi.language is LESSLanguage) {
      astNode.psi.accept(LessPreFixVisitor())
    }
    return textRange
  }

}

/**
 * kotlin代码修正递归访问器
 * @author 吴昊
 * @since 1.3.3
 */
class LessPreFixVisitor() : LessRecursiveVisitor() {

  override fun visitCssDeclaration(declaration: CssDeclaration) {
    val next = declaration.nextIgnoreWs
    if (next !is CssTokenImpl || next.elementType != CssElementTypes.CSS_SEMICOLON) {
      val newSemi = declaration.insertElementAfter(declaration.cssElementFactory.createToken(";", CSSLanguage.INSTANCE))
      newSemi.setBlankLineAfter()
    }
    super.visitCssDeclaration(declaration)
  }

  override fun visitCssElement(element: CssElement) {
    if (element is CssTokenImpl && element.elementType == CssElementTypes.CSS_SEMICOLON) {
      element.setBlankLineAfter()
    }
    super.visitCssElement(element)
  }

  override fun visitCssRuleset(ruleset: CssRuleset) {
    val next = ruleset.nextIgnoreWs
    val prev = ruleset.prevIgnoreWs
    if (next !is CssTokenImpl || next.elementType != CSS_RBRACE) {
      ruleset.setBlankLineAfter(1)
    } else {
      ruleset.setBlankLineAfter()
    }
    if (prev !is CssDeclaration) {
      ruleset.setBlankLineBefore(1)
    } else {
      ruleset.setBlankLineBefore()
    }
    super.visitCssRuleset(ruleset)
  }

  override fun visitCssRulesetList(rulesetList: CssRulesetList?) {
    rulesetList.setBlankLineBefore(1)
    rulesetList.setBlankLineAfter(1)
    super.visitCssRulesetList(rulesetList)
  }

}

