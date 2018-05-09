/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.codeStyle.arrangement.ArrangementSectionDetector
import com.intellij.psi.codeStyle.arrangement.ArrangementSettings
import com.intellij.psi.codeStyle.arrangement.ArrangementUtil
import com.intellij.psi.codeStyle.arrangement.DefaultArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.psi.util.PsiUtil
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.ContainerUtilRt
import com.intellij.util.containers.Stack
import com.wuhao.code.check.KOTLIN_MODIFIERS
import com.wuhao.code.check.processors.EntryType.CLASS
import com.wuhao.code.check.processors.EntryType.ENUM
import com.wuhao.code.check.processors.EntryType.FIELD
import com.wuhao.code.check.processors.EntryType.INIT_BLOCK
import com.wuhao.code.check.processors.EntryType.INTERFACE
import com.wuhao.code.check.processors.EntryType.METHOD
import com.wuhao.code.check.processors.Modifier.ABSTRACT
import com.wuhao.code.check.processors.Modifier.CONST
import com.wuhao.code.check.processors.Modifier.EXTERNAL
import com.wuhao.code.check.processors.Modifier.FINAL
import com.wuhao.code.check.processors.Modifier.INLINE
import com.wuhao.code.check.processors.Modifier.INTERNAL
import com.wuhao.code.check.processors.Modifier.LATEINIT
import com.wuhao.code.check.processors.Modifier.OPEN
import com.wuhao.code.check.processors.Modifier.PACKAGE_PRIVATE
import com.wuhao.code.check.processors.Modifier.PRIVATE
import com.wuhao.code.check.processors.Modifier.PROTECTED
import com.wuhao.code.check.processors.Modifier.PUBLIC
import com.wuhao.code.check.processors.Modifier.SEALED
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifier
import java.util.*

/**
 * @author 吴昊
 * @since 1.2.7
 */
class KotlinArrangementVisitor(private val myInfo: KotlinArrangementParseInfo,
                               private val myDocument: Document?,
                               private val myRanges: Collection<TextRange>,
                               settings: ArrangementSettings) : KotlinRecursiveVisitor() {

  private val myStack = Stack<KotlinElementArrangementEntry>()
  private val myEntries = HashMap<PsiElement, KotlinElementArrangementEntry>()
  private val myGroupingRules: Set<ArrangementSettingsToken>
  private val myMethodBodyProcessor: MethodBodyProcessor
  private val mySectionDetector: ArrangementSectionDetector
  private val myProcessedSectionsComments = ContainerUtil.newHashSet<PsiComment>()

  private val current: DefaultArrangementEntry?
    get() = if (myStack.isEmpty()) null else myStack.peek()

  init {
    myGroupingRules = getGroupingRules(settings)
    myMethodBodyProcessor = MethodBodyProcessor()
    mySectionDetector = ArrangementSectionDetector(myDocument, settings) { data ->
      val range = data.textRange
      val entry = KotlinSectionArrangementEntry(current, data.token, range, data.text, true)
      registerEntry(data.element, entry)
    }
  }

  private fun registerEntry(element: PsiElement, entry: KotlinElementArrangementEntry) {
    myEntries[element] = entry
    val current = current
    if (current == null) {
      myInfo.addEntry(entry)
    } else {
      current.addChild(entry)
    }
  }

  override fun visitComment(comment: PsiComment) {
    if (myProcessedSectionsComments.contains(comment)) {
      return
    }
    mySectionDetector.processComment(comment)
  }

  override fun visitClass(clazz: KtClass,data:Any?) {
    val isSectionCommentsDetected = registerSectionComments(clazz)
    val range = if (isSectionCommentsDetected) getElementRangeWithoutComments(clazz) else clazz.textRange
    var type = CLASS
    if (clazz.isEnum()) {
      type = ENUM
    } else if (clazz.isInterface()) {
      type = INTERFACE
    }
    val entry = createNewEntry(clazz, range, type, clazz.name, true)
    processEntry(entry, clazz, clazz)
  }

  private fun registerSectionComments(element: PsiElement): Boolean {
    val comments = getComments(element)
    var isSectionCommentsDetected = false
    for (comment in comments) {
      if (mySectionDetector.processComment(comment)) {
        isSectionCommentsDetected = true
        myProcessedSectionsComments.add(comment)
      }
    }
    return isSectionCommentsDetected
  }

  private fun createNewEntry(element: PsiElement,
                             range: TextRange,
                             type: ArrangementSettingsToken,
                             name: String?,
                             canArrange: Boolean): KotlinElementArrangementEntry? {
    if (!isWithinBounds(range)) {
      return null
    }
    val current = this.current
    val entry: KotlinElementArrangementEntry
    entry = if (canArrange) {
      val expandedRange = if (myDocument == null) null else ArrangementUtil.expandToLineIfPossible(range, myDocument)
      val rangeToUse = expandedRange ?: range
      KotlinElementArrangementEntry(current, rangeToUse, type, name, true)
    } else {
      KotlinElementArrangementEntry(current, range, type, name, false)
    }
    registerEntry(element, entry)
    return entry
  }

  private fun processEntry(entry: KotlinElementArrangementEntry?,
                           modifier: KtModifierListOwner,
                           nextPsiRoot: PsiElement?) {
    if (entry == null) {
      return
    }
    parseModifiers(modifier.modifierList, entry)
    if (nextPsiRoot == null) {
      return
    }
    processChildrenWithinEntryScope(entry, Runnable { nextPsiRoot.acceptChildren(this) })
  }

  private fun isWithinBounds(range: TextRange): Boolean {
    for (textRange in myRanges) {
      if (textRange.intersects(range)) {
        return true
      }
    }
    return false
  }

  private fun processChildrenWithinEntryScope(entry: KotlinElementArrangementEntry, childrenProcessing: Runnable) {
    myStack.push(entry)
    try {
      childrenProcessing.run()
    } finally {
      myStack.pop()
    }
  }

  override fun visitProperty(property: KtProperty, data: Any?) {
    if (property.parent !is KtClassBody){
      return
    }
    val isSectionCommentsDetected = registerSectionComments(property)
    // There is a possible case that more than one field is declared for the same type like 'int i, j;'. We want to process only
    // the first one then.
    val fieldPrev = getPreviousNonWsComment(property.prevSibling, 0)
    if (PsiUtil.isJavaToken(fieldPrev, JavaTokenType.COMMA)) {
      return
    }
    // There is a possible case that fields which share the same type declaration are located on different document lines, e.g.:
    //    int i1,
    //        i2;
    // We want to consider only the first declaration then but need to expand its range to all affected lines (up to semicolon).
    var range = if (isSectionCommentsDetected) getElementRangeWithoutComments(property) else property.textRange
    val child = property.lastChild
    var needSpecialProcessing = true
    if (isSemicolon(child)) {
      needSpecialProcessing = false
    } else if (child is PsiComment) {
      // There is a possible field definition like below:
      //   int f; // my comment.
      // The comment goes into field PSI here, that's why we need to handle it properly.
      val prev = getPreviousNonWsComment(child, range.startOffset)
      needSpecialProcessing = prev != null && !isSemicolon(prev)
    }

    if (needSpecialProcessing) {
      var e: PsiElement? = property.nextSibling
      while (e != null) {
        if (e is PsiWhiteSpace || e is PsiComment) { // Skip white space and comment
          e = e.nextSibling
          continue
        }
        if (e is PsiJavaToken) {
          if (e.tokenType === JavaTokenType.COMMA) { // Skip comma
            e = e.nextSibling
            continue
          } else {
            break
          }
        }
        if (e is PsiField) {
          var c: PsiElement? = e.lastChild
          if (c != null) {
            c = getPreviousNonWsComment(c, range.startOffset)
          }
          // Stop if current field ends by a semicolon.
          if (c is PsiErrorElement // Incomplete field without trailing semicolon
              || PsiUtil.isJavaToken(c, JavaTokenType.SEMICOLON)) {
            range = TextRange.create(range.startOffset, expandToCommentIfPossible(c!!))
          } else {
            e = e.nextSibling
            continue
          }
        }
        break
      }
    }
    val entry = createNewEntry(property, range, FIELD, property.name, true) ?: return
    processEntry(entry, property, property.initializer)
    myInfo.onFieldEntryCreated(property, entry)
  }

  private fun expandToCommentIfPossible(element: PsiElement): Int {
    if (myDocument == null) {
      return element.textRange.endOffset
    }
    val text = myDocument.charsSequence
    var e: PsiElement? = element.nextSibling
    while (e != null) {
      if (e is PsiWhiteSpace) {
        if (hasLineBreak(text, e.textRange)) {
          return element.textRange.endOffset
        }
      } else if (e is PsiComment) {
        if (!hasLineBreak(text, e.textRange)) {
          return e.textRange.endOffset
        }
      } else {
        return element.textRange.endOffset
      }
      e = e.nextSibling
    }
    return element.textRange.endOffset
  }

  override fun visitClassInitializer(initializer: KtClassInitializer,data: Any?) {
    val entry = createNewEntry(initializer, initializer.textRange, INIT_BLOCK, null, true) ?: return
    parseModifiers(initializer.modifierList, entry)
  }

  override fun visitNamedFunction(function: KtNamedFunction, data: Any?) {
    if (function.parent !is KtClassBody){
      return
    }
    val isSectionCommentsDetected = registerSectionComments(function)
    val range = if (isSectionCommentsDetected)
      getElementRangeWithoutComments(function)
    else
      function.textRange
    val type = METHOD
    val entry = createNewEntry(function, range, type, function.name, true) ?: return
    processEntry(entry, function, function.bodyExpression)
    myInfo.onMethodEntryCreated(function, entry)
    val reset = myMethodBodyProcessor.setBaseMethod(function)
    try {
      function.accept(myMethodBodyProcessor)
    } finally {
      if (reset) {
        myMethodBodyProcessor.setBaseMethod(null)
      }
    }
  }


  /**
   * @author 吴昊
   * @since 1.2.6
   */
  private class MethodBodyProcessor internal constructor() :
      JavaRecursiveElementVisitor() {
    private var myBaseMethod: KtNamedFunction? = null

    internal fun setBaseMethod(baseMethod: KtNamedFunction?): Boolean {
      if (baseMethod == null || myBaseMethod == null) {
        myBaseMethod = baseMethod
        return true
      }
      return false
    }
  }

  companion object {

    private val MODIFIERS = ContainerUtilRt.newHashMap<KtModifierKeywordToken, ArrangementSettingsToken>()

    init {
      MODIFIERS[KtTokens.PROTECTED_KEYWORD] = PROTECTED
      MODIFIERS[KtTokens.PRIVATE_KEYWORD] = PRIVATE
      MODIFIERS[KtTokens.OPEN_KEYWORD] = OPEN
      MODIFIERS[KtTokens.LATEINIT_KEYWORD] = LATEINIT
      MODIFIERS[KtTokens.PUBLIC_KEYWORD] = PUBLIC
      MODIFIERS[KtTokens.INTERNAL_KEYWORD] = INTERNAL
      MODIFIERS[KtTokens.INLINE_KEYWORD] = INLINE
      MODIFIERS[KtTokens.FINAL_KEYWORD] = FINAL
      MODIFIERS[KtTokens.SEALED_KEYWORD] = SEALED
      MODIFIERS[KtTokens.ABSTRACT_KEYWORD] = ABSTRACT
      MODIFIERS[KtTokens.CONST_KEYWORD] = CONST
      MODIFIERS[KtTokens.EXTERNAL_KEYWORD] = EXTERNAL
    }

    private fun getGroupingRules(settings: ArrangementSettings): Set<ArrangementSettingsToken> {
      val groupingRules = ContainerUtilRt.newHashSet<ArrangementSettingsToken>()
      for (rule in settings.groupings) {
        groupingRules.add(rule.groupingType)
      }
      return groupingRules
    }

    private fun getElementRangeWithoutComments(element: PsiElement): TextRange {
      val children = element.children
      assert(children.size > 1 && children[0] is PsiComment)

      var i = 0
      var child = children[i]
      while (child is PsiWhiteSpace || child is PsiComment) {
        child = children[++i]
      }

      return TextRange(child.textRange.startOffset, element.textRange.endOffset)
    }

    private fun getComments(element: PsiElement): List<PsiComment> {
      val children = element.children
      val comments = ContainerUtil.newArrayList<PsiComment>()

      for (e in children) {
        if (e is PsiComment) {
          comments.add(e)
        } else if (e !is PsiWhiteSpace) {
          return comments
        }
      }

      return comments
    }

    private fun parseModifiers(modifierList: KtModifierList?, entry: KotlinElementArrangementEntry) {
      if (modifierList == null) {
        return
      }
      for (modifier in KOTLIN_MODIFIERS) {
        if (modifierList.hasModifier(modifier)) {
          val arrangementModifier = MODIFIERS[modifier]
          if (arrangementModifier != null) {
            entry.addModifier(arrangementModifier)
          }
        }
      }
      if (modifierList.visibilityModifier() == null) {
        entry.addModifier(PACKAGE_PRIVATE)
      }
    }

    private fun getPreviousNonWsComment(element: PsiElement?, minOffset: Int): PsiElement? {
      if (element == null) {
        return null
      }
      var e = element
      while (e != null && e.textRange.startOffset >= minOffset) {
        if (e is PsiWhiteSpace || e is PsiComment) {
          e = e.prevSibling
          continue
        }
        return e
      }
      return null
    }

    private fun isSemicolon(e: PsiElement?): Boolean {
      return PsiUtil.isJavaToken(e, JavaTokenType.SEMICOLON)
    }

    private fun hasLineBreak(text: CharSequence, range: TextRange): Boolean {
      var i = range.startOffset
      val end = range.endOffset
      while (i < end) {
        if (text[i] == '\n') {
          return true
        }
        i++
      }
      return false
    }
  }

}

