/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
@file:Suppress("unused")

package com.wuhao.code.check

import com.intellij.ide.DataManager
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList.ModifierType
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.JavaProjectRootsUtil
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl
import com.intellij.psi.*
import com.intellij.psi.css.CssElement
import com.intellij.psi.css.CssElementFactory
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.refactoring.rename.inplace.VariableInplaceRenameHandler
import com.intellij.util.IncorrectOperationException
import com.intellij.util.PlatformUtils
import com.wuhao.code.check.inspection.fix.SpaceQuickFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Position.*
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.isObjectLiteral
import java.io.File

/**
 * @author wuhao
 * @since 0.1
 */
/**  获取psi元素的所有祖先元素，按距离从近到远 */
val PsiElement.ancestors: List<PsiElement>
  get() {
    val ancestors = ArrayList<PsiElement>()
    var el: PsiElement? = this
    while (true) {
      ancestors.add(el!!)
      el = el.parent
      if (el == null) {
        break
      }
    }
    return ancestors
  }

/**  获取kotlin方法的方法体（包含括号） */
val KtNamedFunction.body: KtBlockExpression?
  get() {
    return this.getChildOfType()
  }

/**  获取目录下所有缓存的文件 */
val VirtualDirectoryImpl.cachedPosterity: ArrayList<VirtualFile>
  get() {
    val list = ArrayList<VirtualFile>()
    getCachedChildren(list, this)
    return list
  }

/**  css元素工厂类 */
val CssElement.cssElementFactory: CssElementFactory
  get() {
    return CssElementFactory.getInstance(this.project)
  }

/**  psi元素的深度 */
val PsiElement.depth: Int
  get() {
    var depth = 0
    fun analyzeDepth(children: List<PsiElement>) {
      if (children.isNotEmpty()) {
        depth++
        analyzeDepth(children.map { it.children.toList() }.flatten())
      } else {
        return
      }
    }
    analyzeDepth(this.children.toList())
    return depth
  }

/**  psi元素的结束位置 */
val PsiElement.endOffset: Int
  get() {
    return this.textRange.endOffset
  }

/**  是否是父元素的第一个子元素 */
val PsiElement.isFirstChild: Boolean
  get() {
    return this.parent != null && this.parent.firstChild == this
  }

/**  是否idea */
val isIdea: Boolean
  get() {
    return PlatformUtils.isIdeaCommunity() || PlatformUtils.isIdeaUltimate()
  }

/**  判断kotlin属性是否val */
val KtProperty.isVal: Boolean
  get() {
    return !this.isVar
  }

/**  是否webstorm */
val isWebStorm: Boolean
  get() {
    return PlatformUtils.isWebStorm()
  }

/**  获取kt元素的工厂类 */
val PsiElement.ktPsiFactory: KtPsiFactory
  get() = this.project.ktPsiFactory

/**  获取kt元素的工厂类 */
val Project.ktPsiFactory: KtPsiFactory
  get() {
    if (KT_PSI_FACTORY_CACHE[this] == null) {
      KT_PSI_FACTORY_CACHE[this] = KtPsiFactory(this)
    }
    return KT_PSI_FACTORY_CACHE[this]!!
  }

/**  和当前元素并列的后一个元素 */
val PsiElement.next: PsiElement?
  get() = this.nextSibling

/**  获取当前元素并列的下一个非空白元素 */
val PsiElement.nextIgnoreWs: PsiElement?
  get() {
    var sibling = this.nextSibling
    while (sibling != null && sibling is PsiWhiteSpace) {
      sibling = sibling.nextSibling
    }
    return sibling
  }

/**  获取所有后代元素 */
val PsiElement.posterity: ArrayList<PsiElement>
  get() {
    val list = ArrayList<PsiElement>()
    getChildren(list, this)
    return list
  }

/**  和当前元素并列的前一个元素 */
val PsiElement.prev: PsiElement?
  get() = this.prevSibling

/**  获取当前元素之前的第一个非空白元素 */
val PsiElement.prevIgnoreWs: PsiElement?
  get() {
    var sibling = this.prevSibling
    while (sibling != null && sibling is PsiWhiteSpace) {
      sibling = sibling.prevSibling
    }
    return sibling
  }

/**
 * 获取java psi元素的工厂类
 * @return java psi元素的工厂类
 */
val PsiElement.psiElementFactory: PsiElementFactory
  get() {
    return this.project.psiElementFactory
  }

/**  psifactory */
val Project.psiElementFactory: PsiElementFactory
  get() {
    if (PSI_ELEMENT_FACTORY_CACHE[this] == null) {
      PSI_ELEMENT_FACTORY_CACHE[this] = PsiElementFactory.SERVICE.getInstance(this)
    }
    return PSI_ELEMENT_FACTORY_CACHE[this]!!
  }

/**  元素起始位置 */
val PsiElement.startOffset: Int
  get() {
    return this.textRange.startOffset
  }

/**  是否添加了vuejs的支持 */
val vueEnabled: Boolean
  get() {
    return try {
      Class.forName("org.jetbrains.vuejs.VueLanguage")
      true
    } catch (e: Exception) {
      false
    }
  }

private val KT_PSI_FACTORY_CACHE = HashMap<Project, KtPsiFactory>()

private val PSI_ELEMENT_FACTORY_CACHE = HashMap<Project, PsiElementFactory>()

/**
 * 获取指定类型的最近的祖先元素
 */
inline fun <reified T> PsiElement.ancestorOfType(): T? {
  var el: PsiElement? = this.parent
  while (el != null && el !is T) {
    el = el.parent
  }
  return el as T?
}

/**
 * 清除空白行
 * @param position 需要清除的位置
 */
fun PsiElement.clearBlankLineBeforeOrAfter(position: SpaceQuickFix.Position) {
  val whiteSpaceEl = when (position) {
    Before -> this.prev
    After  -> this.next
    else   -> null
  }
  if (whiteSpaceEl !is PsiWhiteSpace) {
    if (position == Before) {
      this.insertElementBefore(project.createNewLine())
    } else if (position == After) {
      this.insertElementAfter(project.createNewLine())
    }
  } else if (whiteSpaceEl.getLineCount() != 1) {
    whiteSpaceEl.replace(project.createNewLine())
  }
}

/**
 *
 * @param content
 * @return
 */
fun KtPsiFactory.createDocSection(content: String): KDocSection {
  val docSection = this.createComment("""/**
    |$content
    | */
  """.trimMargin()).getChildOfType<KDocSection>()
  if (docSection != null) {
    return docSection
  } else {
    throw IncorrectOperationException("Incorrect doc section")
  }
}

/**
 * 创建注释标签元素
 * @param content
 * @return
 */
fun KtPsiFactory.createDocTag(tag: String, content: String): KDocTag {
  val docTag = this.createComment("""/**
    | * @$tag $content
    | */
  """.trimMargin()).getChildOfType<KDocSection>()
      ?.getChildOfType<KDocTag>()
  if (docTag != null) {
    return docTag
  } else {
    throw IncorrectOperationException("Incorrect doc section text")
  }
}

/**
 * 获取空行元素
 */
fun Project.createNewLine(n: Int = 1): PsiWhiteSpace {
  return createWhiteSpace("\n".repeat(n))
}

/**
 * 创建空白行元素
 * @param count 换行数
 * @return 空白行元素
 */
fun PsiElement.createNewLine(count: Int = 1): PsiWhiteSpace {
  return project.createNewLine(count)
}

/**
 * 获取空白元素
 */
fun Project.createWhiteSpace(str: String): PsiWhiteSpace {
  return PsiFileFactory.getInstance(this)
      .createFileFromText(JavascriptLanguage.INSTANCE, str).children.first() as PsiWhiteSpace
}

/**
 *
 * @param str
 * @return
 */
fun PsiElement.createWhiteSpace(str: String = " "): PsiWhiteSpace {
  return project.createWhiteSpace(str)
}

/**
 *
 * @return
 */
fun PsiElement.findExistingEditor(): Editor? {
  val file = containingFile?.virtualFile ?: return null
  val document = FileDocumentManager.getInstance().getDocument(file) ?: return null

  val editorFactory = EditorFactory.getInstance()

  val editors = editorFactory.getEditors(document)
  return if (editors.isEmpty()) {
    null
  } else {
    editors[0]
  }
}

/**
 * 查找文件中指定名称的对象类
 * @param name 类名称
 * @return 如果找到对应的对象类返回true，反之false
 */
fun KtFile.findObjectClass(name: String): Boolean {
  return this.classes.any {
    it.name == name && it is KtClassOrObject && it.isObjectLiteral()
  }
}

/**
 * 根据类名获取psi file
 * @param className 类名
 * @return
 */
fun Project.findPsiFile(className: String?): PsiFile? {
  if (className != null) {
    val sourceRoots = JavaProjectRootsUtil.getSuitableDestinationSourceRoots(this)
    val psiManager = PsiManager.getInstance(this)
    sourceRoots.forEach {
      val classFile = findSourceFile(it, className)
      if (classFile != null) {
        return psiManager.findFile(classFile) ?: classFile.toPsiFile(this)
      }
    }
  }
  return null
}

/**
 * 选择符合条件的第一个子元素
 * @param predicate 筛选条件
 * @return 符合条件的第一个子元素
 */
fun PsiElement.firstChild(predicate: (PsiElement) -> Boolean): PsiElement? {
  return this.children.firstOrNull(predicate)
}

/**
 * 按距离获取祖先元素，0为parent，如果没有找到则返回null
 * @param level 距离
 */
fun PsiElement.getAncestor(level: Int): PsiElement? {
  var el: PsiElement? = this
  for (i in 0 until level) {
    el = el?.parent
    if (el == null) {
      return null
    }
  }
  return el
}

/**
 * 获取指定类型的所有的祖先元素
 */
inline fun <reified T> PsiElement.getAncestorsOfType(): List<T> {
  val result = arrayListOf<T>()
  var el: PsiElement? = this.parent
  while (el != null) {
    if (el is T) {
      result.add(el)
    }
    el = el.parent
  }
  return result.toList()
}

/**
 * 获取指定类型的距离当前元素最近的祖先元素
 */
inline fun <reified T> PsiElement.getAncestorOfType(): T? {
  var el: PsiElement? = this.parent
  while (el != null) {
    if (el is T) {
      return el
    }
    el = el.parent
  }
  return null
}


/**
 * 判断kotlin元素上指定名称的注解
 * @param annotation 指定注解名称
 * @return
 */
fun KtAnnotated.getAnnotation(annotation: String): KtAnnotationEntry? {
  return this.annotationEntries.firstOrNull { it.toLightAnnotation()?.qualifiedName == annotation }
}

/**
 *
 * @return
 */
inline fun <reified T> PsiElement.getChildByType(): T? {
  return this.children.firstOrNull { it is T } as T?
}

/**
 * 获取连续的指定类型的所有的祖先元素
 */
inline fun <reified T> PsiElement.getContinuousAncestorsMatches(
    predicate: (PsiElement) -> Boolean
): ArrayList<T> {
  val result = arrayListOf<T>()
  var el: PsiElement? = this.parent
  while (el != null && el is T && predicate(el)) {
    result.add(el)
    el = el.parent
  }
  return result
}

/**
 * 获取连续的指定类型的所有的祖先元素
 */
inline fun <reified T> PsiElement.getContinuousAncestorsOfType(): ArrayList<T> {
  val result = arrayListOf<T>()
  var el: PsiElement? = this.parent
  while (el != null && el is T) {
    result.add(el)
    el = el.parent
  }
  return result
}

/**
 * 获取元素占用的行数
 * @return
 */
fun PsiElement.getLineCount(): Int {
  val doc = containingFile?.let { file -> PsiDocumentManager.getInstance(project).getDocument(file) }
  if (doc != null) {
    val spaceRange = textRange ?: TextRange.EMPTY_RANGE
    if (spaceRange.endOffset <= doc.textLength) {
      val startLine = doc.getLineNumber(spaceRange.startOffset)
      val endLine = doc.getLineNumber(spaceRange.endOffset)
      return endLine - startLine
    }
  }
  return (text ?: "").count { it == '\n' } + 1
}

/**
 * 获取当前元素之前连续的同类型元素
 * @return 符合条件的同类型元素
 */
inline fun <reified T> PsiElement.getPrevContinuousSiblingsOfType(): ArrayList<T> {
  var sibling = this.prevSibling
  val result = arrayListOf<T>()
  while (sibling != null && sibling is T) {
    result.add(sibling)
    sibling = sibling.prevSibling
  }
  return result
}

/**
 * 获取当前元素之前连续的同类型元素，且排除空白元素
 * @return 符合条件的同类型元素
 */
inline fun <reified T> PsiElement.getPrevContinuousSiblingsOfTypeIgnoreWhitespace(): ArrayList<T> {
  var sibling = this.prevSibling
  val result = arrayListOf<T>()
  while (sibling != null && (sibling is T || sibling is PsiWhiteSpace)) {
    if (sibling is T) {
      result.add(sibling)
    }
    sibling = sibling.prevSibling
  }
  return result
}

/**
 *
 * @return
 */
fun PsiElement.getPrevSiblings(): List<PsiElement> {
  var sibling = this.prevSibling
  val list = arrayListOf<PsiElement>()
  while (sibling != null) {
    list.add(sibling)
    sibling = sibling.prevSibling
  }
  return list
}

/**
 * 获取当前元素之前指定类型的所有同级元素
 * @return 符合条件的元素结合
 */
inline fun <reified T> PsiElement.getPrevSiblingsOfType(): List<T> {
  var sibling = this.prevSibling
  val list = arrayListOf<T>()
  while (sibling != null) {
    if (sibling is T) {
      list.add(sibling)
    }
    sibling = sibling.prevSibling
  }
  return list
}

/**
 * 获取指定类型的同级元素
 */
inline fun <reified T> PsiElement.getSiblingsOfType(): List<PsiElement> {
  if (this.parent == null) {
    return listOf()
  } else {
    return this.parent.children.filter { it is T }
  }
}

/**
 * 读取项目版本号，目前支持maven
 */
fun Project.getVersion(): String? {
  val mavenProjectsManager = MavenProjectsManager.getInstance(this)
  if (mavenProjectsManager != null && mavenProjectsManager.hasProjects()) {
    val mavenProject = mavenProjectsManager.projects.firstOrNull()
    if (mavenProject != null) {
      return mavenProject.modelMap["version"]
    }
  }
  return null
}

/**
 * 判断元素是否带有指定注解
 * @param annotation 指定注解名称
 * @return
 */
fun KtAnnotated.hasAnnotation(annotation: String): Boolean {
  return this.annotationEntries.any { it.toLightAnnotation()?.qualifiedName == annotation }
}

/**
 * 是否包含指定名称的装饰器
 */
fun JSAttributeListOwner.hasDecorator(name: String): Boolean {
  return this.attributeList?.decorators?.any { it.decoratorName == name } ?: false
}

/**
 * 判断kotlin方式是否有注释
 */
fun KtNamedFunction.hasDoc(): Boolean {
  return this.firstChild is KDoc
}

/**
 * 是否是指定类型的元素
 */
fun PsiElement?.hasElementType(type: IElementType): Boolean {
  return this is LeafPsiElement && this.elementType == type
}

/**
 * 是否包含指定名称的修饰符
 */
fun JSAttributeListOwner.hasModifier(modifier: ModifierType): Boolean {
  return this.attributeList?.hasModifier(modifier) ?: false
}

/**
 *
 * @param name
 * @return
 */
fun KtAnnotated.hasSuppress(name: String): Boolean {
  val annotated = this.ancestors.filter { it is KtAnnotated }
      .map { it as KtAnnotated }
  annotated.forEach { it ->
    val entry = it.annotationEntries.firstOrNull { it.typeReference?.text == "Suppress" || it.typeReference?.text == "kotlin.Suppress" }
    if (entry != null) {
      return entry.valueArguments.map {
        it.getArgumentExpression()?.children?.firstOrNull()?.text
      }.contains(name)
    }
  }
  return false
}

/**
 * 注释中是否包含指定标签
 * @param tag 指定的标签
 * @return 包含标签返回true，反之false
 */
fun KDocSection.hasTag(tag: KDocKnownTag): Boolean {
  return getChildrenOfType<KDocTag>().any { it.knownTag == tag }
}

/**
 * 将当前psi元素插入到指定元素后面
 * @param element 指定的元素
 */
fun PsiElement.insertAfter(element: PsiElement): PsiElement {
  return element.insertElementAfter(this)
}

/**
 * 将当前psi元素插入到指定元素前面
 * @param element 指定的元素
 */
fun PsiElement.insertBefore(element: PsiElement): PsiElement {
  return element.insertElementBefore(this)
}

/**
 * 在当前psi元素后插入元素
 * @param element 待插入的元素
 */
fun PsiElement.insertElementAfter(element: PsiElement): PsiElement {
  return this.parent.addAfter(element, this)
}

/**
 * 在当前psi元素前插入元素
 * @param element 待插入的元素
 */
fun PsiElement.insertElementBefore(element: PsiElement): PsiElement {
  return this.parent.addBefore(element, this)
}

/**
 * 在当前元素前面插入多个元素
 * @param elements 待插入的元素
 */
fun PsiElement.insertElementsBefore(vararg elements: PsiElement) {
  elements.forEach {
    this.insertElementBefore(it)
  }
}

/**
 * 取第一个特定类型的子元素
 */
inline fun <reified T> PsiElement.isFirstChildOfType(): Boolean {
  return this.parent != null && this.parent.children.firstOrNull { it is T } == this
}

/**
 * 判断kotlin方法是否接口方法
 * @return
 */
fun KtNamedFunction.isInterfaceFun(): Boolean {
  val containingClass = this.containingClass()
  return containingClass != null && containingClass.isInterface()
}

/**
 * 是否多行
 * @return
 */
fun PsiElement.isMultiLine(): Boolean = getLineCount() > 1

/**
 * 移动光标
 * @param offset
 */
fun Editor.moveCaret(offset: Int) {
  this.caretModel.moveToOffset(offset)
}

/**
 * 重命名元素
 * @param element 待重命名的元素
 */
fun renameElement(element: PsiElement,
                  caretOffset: Int = -1,
                  parentElement: PsiElement? = null,
                  indexInParent: Int = -1) {
  DataManager.getInstance().dataContextFromFocusAsync.onSuccess {
    val editor = it.getData(CommonDataKeys.EDITOR)!!
    PsiDocumentManager.getInstance(element.project).doPostponedOperationsAndUnblockDocument(editor.document)
    val realElement = if (parentElement != null && indexInParent >= 0) {
      parentElement.children[indexInParent]
    } else {
      element
    }
    if (caretOffset < 0) {
      editor.moveCaret(realElement.startOffset)
    } else {
      editor.moveCaret(caretOffset)
    }
    editor.settings.isVariableInplaceRenameEnabled = true
    val handler = VariableInplaceRenameHandler()
    handler.invoke(realElement.project, editor, realElement.containingFile, it)
  }
}

/**
 * 在当前元素后面添加空行
 * @param blankLines 空白行数
 */
fun PsiElement.setBlankLineAfter(blankLines: Int = 0) {
  setBlankLine(blankLines, After)
}

/**
 * 在当前元素前面添加空行
 * @param blankLines 空白行数
 */
fun PsiElement.setBlankLineBefore(blankLines: Int = 0) {
  setBlankLine(blankLines, Before)
}

/**
 * 在当前元素前后添加空行
 * @param blankLines 空白行数
 */
fun PsiElement.setBlankLineBoth(blankLines: Int = 0) {
  setBlankLine(blankLines, Both)
}

/**
 * 文件转化为psi文件
 */
fun File.toPsiFile(project: Project): PsiFile? = toVirtualFile()?.toPsiFile(project)

/**
 * 虚拟文件转psi文件
 */
fun VirtualFile.toPsiFile(project: Project): PsiFile? = PsiManager.getInstance(project).findFile(this)

/**
 * 文件转化为虚拟文件
 * @return
 */
fun File.toVirtualFile(): VirtualFile? = LocalFileSystem.getInstance().findFileByIoFile(this)

/**
 * 根据类名查找源文件
 * @param root
 * @param className
 * @return
 */
private fun findSourceFile(root: VirtualFile, className: String): VirtualFile? {
  val nameWithoutExtension = "/" + className.replace(".", "/")
  return root.findFileByRelativePath("$nameWithoutExtension.java")
      ?: root.findFileByRelativePath("$nameWithoutExtension.kt")
}

/**
 *
 * @param list
 * @param virtualDirectoryImpl
 */
private fun getCachedChildren(list: ArrayList<VirtualFile>, virtualDirectoryImpl: VirtualDirectoryImpl) {
  list.addAll(virtualDirectoryImpl.cachedChildren.filter { !it.isDirectory })
  virtualDirectoryImpl.cachedChildren.filter { it is VirtualDirectoryImpl }
      .forEach {
        getCachedChildren(list, it as VirtualDirectoryImpl)
      }
}

/**
 *
 * @param list
 * @param psiElement
 */
private fun getChildren(list: ArrayList<PsiElement>, psiElement: PsiElement) {
  if (psiElement.children.isNotEmpty()) {
    list.addAll(psiElement.children)
    psiElement.children.forEach {
      getChildren(list, it)
    }
  }
}

/**
 * 在当前元素的指定位置添加空行
 * @param blankLines 空白行数
 * @param position 添加空白行的位置
 */
private fun PsiElement.setBlankLine(blankLines: Int, position: SpaceQuickFix.Position) {
  val lineBreaks = blankLines + 1
  if (position == Before || position == Both) {
    val prev = this.prev
    if (prev !is PsiWhiteSpace?) {
      this.insertElementBefore(this.project.createNewLine(lineBreaks))
    } else if (prev != null && prev.getLineCount() != lineBreaks) {
      prev.replace(this.project.createNewLine(lineBreaks))
    }
  }
  if (position == After || position == Both) {
    val next = this.next
    if (next != null && next !is PsiWhiteSpace) {
      this.insertElementAfter(this.project.createNewLine(lineBreaks))
    } else if (next != null && next.getLineCount() != lineBreaks) {
      next.replace(this.project.createNewLine(lineBreaks))
    }
  }
}
