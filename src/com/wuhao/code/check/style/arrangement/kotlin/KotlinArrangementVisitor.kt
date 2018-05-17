/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.kotlin

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.codeStyle.arrangement.ArrangementSectionDetector
import com.intellij.psi.codeStyle.arrangement.ArrangementSettings
import com.intellij.psi.codeStyle.arrangement.ArrangementUtil
import com.intellij.psi.codeStyle.arrangement.DefaultArrangementEntry
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.psi.util.PsiUtil
import com.intellij.util.Functions
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.ContainerUtilRt
import com.intellij.util.containers.Stack
import com.wuhao.code.check.KOTLIN_MODIFIERS
import com.wuhao.code.check.style.KotlinEntryType.CLASS
import com.wuhao.code.check.style.KotlinEntryType.COMPANION_OBJECT
import com.wuhao.code.check.style.KotlinEntryType.DATA_CLASS
import com.wuhao.code.check.style.KotlinEntryType.ENUM
import com.wuhao.code.check.style.KotlinEntryType.ENUM_ENTRY
import com.wuhao.code.check.style.KotlinEntryType.FUNCTION
import com.wuhao.code.check.style.KotlinEntryType.INIT_BLOCK
import com.wuhao.code.check.style.KotlinEntryType.INTERFACE
import com.wuhao.code.check.style.KotlinEntryType.OBJECT
import com.wuhao.code.check.style.KotlinEntryType.PROPERTY
import com.wuhao.code.check.style.KotlinEntryType.SECONDARY_CONSTRUCTOR
import com.wuhao.code.check.style.KotlinModifier.ABSTRACT
import com.wuhao.code.check.style.KotlinModifier.CONST
import com.wuhao.code.check.style.KotlinModifier.EXTERNAL
import com.wuhao.code.check.style.KotlinModifier.FINAL
import com.wuhao.code.check.style.KotlinModifier.INLINE
import com.wuhao.code.check.style.KotlinModifier.INTERNAL
import com.wuhao.code.check.style.KotlinModifier.LATEINIT
import com.wuhao.code.check.style.KotlinModifier.OPEN
import com.wuhao.code.check.style.KotlinModifier.PACKAGE_PRIVATE
import com.wuhao.code.check.style.KotlinModifier.PRIVATE
import com.wuhao.code.check.style.KotlinModifier.PROTECTED
import com.wuhao.code.check.style.KotlinModifier.PUBLIC
import com.wuhao.code.check.style.KotlinModifier.SEALED
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.idea.refactoring.isCompanionMemberOf
import org.jetbrains.kotlin.idea.references.resolveMainReferenceToDescriptors
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifier
import org.jetbrains.kotlin.resolve.source.getPsi
import java.util.*

/**
 * @author 吴昊
 * @since 1.2.7
 */
class KotlinArrangementVisitor(private val myInfo: KotlinArrangementParseInfo,
                               private val myDocument: Document?,
                               private val myRanges: Collection<TextRange>,
                               settings: ArrangementSettings) : KotlinRecursiveVisitor() {

  private val current: DefaultArrangementEntry?
    get() = if (myStack.isEmpty()) {
      null
    } else {
      myStack.peek()
    }
  private val myCachedClassProperties = ContainerUtil.newHashMap<KtClass, Set<KtProperty>>()
  private val myCachedCompanionClassProperties = ContainerUtil.newHashMap<KtClass, Set<KtProperty>>()
  private val myEntries = HashMap<PsiElement, KotlinElementArrangementEntry>()
  private val mySectionDetector: ArrangementSectionDetector = ArrangementSectionDetector(myDocument, settings) { data ->
    val range = data.textRange
    val entry = KotlinSectionArrangementEntry(current, data.token, range, data.text, true)
    registerEntry(data.element, entry)
  }
  private val myMethodBodyProcessor: MethodBodyProcessor = MethodBodyProcessor()
  private val myObjectBodyProcessor: ObjectBodyProcessor = ObjectBodyProcessor()
  private val myProcessedSectionsComments = ContainerUtil.newHashSet<PsiComment>()
  private val myStack = Stack<KotlinElementArrangementEntry>()

  companion object {
    const val MAX_METHOD_LOOKUP_DEPTH = 3

    private val MODIFIERS = ContainerUtilRt.newHashMap<KtModifierKeywordToken, ArrangementSettingsToken>().apply {
      put(KtTokens.PROTECTED_KEYWORD, PROTECTED)
      put(KtTokens.PRIVATE_KEYWORD, PRIVATE)
      put(KtTokens.OPEN_KEYWORD, OPEN)
      put(KtTokens.LATEINIT_KEYWORD, LATEINIT)
      put(KtTokens.PUBLIC_KEYWORD, PUBLIC)
      put(KtTokens.INTERNAL_KEYWORD, INTERNAL)
      put(KtTokens.INLINE_KEYWORD, INLINE)
      put(KtTokens.FINAL_KEYWORD, FINAL)
      put(KtTokens.SEALED_KEYWORD, SEALED)
      put(KtTokens.ABSTRACT_KEYWORD, ABSTRACT)
      put(KtTokens.CONST_KEYWORD, CONST)
      put(KtTokens.EXTERNAL_KEYWORD, EXTERNAL)
    }

    init {

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
  }

  override fun visitClass(clazz: KtClass, data: Any?) {
    // 不对枚举元素排序
    if (clazz is KtEnumEntry) {
      return
    }
    val isSectionCommentsDetected = registerSectionComments(clazz)
    val range = if (isSectionCommentsDetected) {
      getElementRangeWithoutComments(clazz)
    } else {
      clazz.textRange
    }
    val type = when {
      clazz.isEnum() -> ENUM
      clazz.isInterface() -> INTERFACE
      clazz.isData() -> DATA_CLASS
      clazz is KtEnumEntry -> ENUM_ENTRY
      else -> CLASS
    }
    val entry = createNewEntry(clazz, range, type, clazz.name, true)
    processEntry(entry, clazz, clazz)
  }

  override fun visitClassInitializer(initializer: KtClassInitializer, data: Any?) {
    val entry = createNewEntry(initializer, initializer.textRange, INIT_BLOCK, null, true) ?: return
    processEntry(entry, initializer, null)
  }

  override fun visitComment(comment: PsiComment) {
    if (myProcessedSectionsComments.contains(comment)) {
      return
    }
    mySectionDetector.processComment(comment)
  }

  override fun visitNamedFunction(function: KtNamedFunction, data: Any?) {
    if (function.parent !is KtClassBody) {
      return
    }
    val isSectionCommentsDetected = registerSectionComments(function)
    val range = if (isSectionCommentsDetected) {
      getElementRangeWithoutComments(function)
    } else {
      function.textRange
    }
    val type = FUNCTION
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

  override fun visitObjectDeclaration(declaration: KtObjectDeclaration, data: Any?) {
    val range = declaration.textRange
    val type = if (declaration.isCompanion()) {
      COMPANION_OBJECT
    } else {
      OBJECT
    }
    val entry = createNewEntry(declaration, range, type, null, true) ?: return
    processEntry(entry, declaration, declaration.getBody())
    val reset = myObjectBodyProcessor.setBaseObject(declaration)
    try {
      declaration.accept(myObjectBodyProcessor)
    } finally {
      if (reset) {
        myObjectBodyProcessor.setBaseObject(null)
      }
    }
  }

  override fun visitProperty(property: KtProperty, data: Any?) {
    val isSectionCommentsDetected = registerSectionComments(property)
    if (property.isLocal || property.isTopLevel) {
      return
    }
    // There is a possible case that fields which share the same type declaration are located on different document lines, e.g.:
    //    int i1,
    //        i2;
    // We want to consider only the first declaration then but need to expand its range to all affected lines (up to semicolon).
    var range = if (isSectionCommentsDetected) {
      getElementRangeWithoutComments(property)
    } else {
      property.textRange
    }
    val child = property.lastChild
    var needSpecialProcessing = true
    if (child is PsiComment) {
      // There is a possible field definition like below:
      //   int f; // my comment.
      // The comment goes into field PSI here, that's why we need to handle it properly.
      val prev = getPreviousNonWsComment(child, range.startOffset)
      needSpecialProcessing = prev != null
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
        if (e is KtProperty) {
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
    val entry = createNewEntry(property, range, PROPERTY, property.name, true) ?: return
    processEntry(entry, property, property.initializer)
    myInfo.onPropertyEntryCreated(property, entry)
    val referencedFields = getReferencedProperties(property)
    for (referencedField in referencedFields) {
      myInfo.registerPropertyInitializationDependency(property, referencedField)
    }
  }

  override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor, data: Any?) {
    val entry = createNewEntry(constructor, constructor.textRange, SECONDARY_CONSTRUCTOR, null, true)
    processEntry(entry, constructor, null)
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
      val expandedRange = if (myDocument == null) {
        null
      } else {
        ArrangementUtil.expandToLineIfPossible(range, myDocument)
      }
      val rangeToUse = expandedRange ?: range
      KotlinElementArrangementEntry(current, rangeToUse, type, name, true)
    } else {
      KotlinElementArrangementEntry(current, range, type, name, false)
    }
    registerEntry(element, entry)
    return entry
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

  private fun getReferencedProperties(property: KtProperty): List<KtProperty> {
    val referencedElements = ArrayList<KtProperty>()
    val propertyInitializer = property.initializer
    val containingClass = property.containingClass()
    val containingObject = property.containingClassOrObject
    if (propertyInitializer == null || (containingClass == null && containingObject == null)) {
      return referencedElements
    }
    val isCompanionProperty = when {
      containingClass != null -> property.isCompanionMemberOf(containingClass)
      else -> false
    }
    val classProperties =
        if (containingObject is KtObjectDeclaration) {
          containingObject.getBody()?.properties ?: setOf()
        } else if (isCompanionProperty) {
          var classProperties: Set<KtProperty>? = myCachedCompanionClassProperties[containingClass]
          if (classProperties == null) {
            classProperties = ContainerUtil.map2Set(containingClass!!.companionObjects.mapNotNull {
              it.getBody()?.properties
            }.flatten(), Functions.id())
            myCachedCompanionClassProperties[containingClass] = classProperties
          }
          classProperties
        } else {
          var classProperties: Set<KtProperty>? = myCachedClassProperties[containingClass]
          if (classProperties == null) {
            classProperties = ContainerUtil.map2Set(containingClass!!.getProperties(), Functions.id())
            myCachedClassProperties[containingClass] = classProperties
          }
          classProperties
        }
    propertyInitializer.accept(object : KotlinRecursiveVisitor() {

      internal var myCurrentMethodLookupDepth: Int = 0

      override fun visitReferenceExpression(expression: KtReferenceExpression, data: Any?) {
        val refs = expression.resolveMainReferenceToDescriptors()
        refs.forEach { ref ->
          if (ref is PropertyDescriptor) {
            val psi = ref.source.getPsi()
            if (psi is KtProperty && classProperties.contains(psi)) {
              referencedElements.add(psi)
            }
          } else if (ref is FunctionDescriptor) {
            val psi = ref.source.getPsi()
            if (psi is KtNamedFunction && myCurrentMethodLookupDepth < MAX_METHOD_LOOKUP_DEPTH) {
              myCurrentMethodLookupDepth++
              visitNamedFunction(psi, data)
              myCurrentMethodLookupDepth--
            }
          }
        }
        super.visitReferenceExpression(expression, data)
      }

    })
    return referencedElements
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

  private fun registerEntry(element: PsiElement, entry: KotlinElementArrangementEntry) {
    myEntries[element] = entry
    val current = current
    if (current == null) {
      myInfo.addEntry(entry)
    } else {
      current.addChild(entry)
    }
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

  /**
   * @author 吴昊
   * @since 1.2.6
   */
  private class MethodBodyProcessor internal constructor() :
      KotlinRecursiveVisitor() {

    private var myBaseMethod: KtNamedFunction? = null

    internal fun setBaseMethod(baseMethod: KtNamedFunction?): Boolean {
      if (baseMethod == null || myBaseMethod == null) {
        myBaseMethod = baseMethod
        return true
      }
      return false
    }

  }

  /**
   * @author 吴昊
   * @since 1.2.6
   */
  private class ObjectBodyProcessor internal constructor() :
      KotlinRecursiveVisitor() {

    private var myBaseObject: KtObjectDeclaration? = null

    internal fun setBaseObject(baseObject: KtObjectDeclaration?): Boolean {
      if (baseObject == null || myBaseObject == null) {
        myBaseObject = baseObject
        return true
      }
      return false
    }

  }

}

