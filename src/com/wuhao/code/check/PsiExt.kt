/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.ProjectCoreUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.PsiElementFactoryImpl
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.refactoring.rename.inplace.VariableInplaceRenameHandler
import org.jetbrains.kotlin.idea.core.moveCaret
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * 获取psi元素的所有祖先元素，按距离从近到远
 */
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

/**
 * 获取当前元素之前的第一个非空白元素
 */
val PsiElement.prevSiblingIgnoreWhitespace: PsiElement?
  get() {
    var sibling = this.prevSibling
    while (sibling != null && sibling is PsiWhiteSpace) {
      sibling = sibling.prevSibling
    }
    return sibling
  }

/**
 * 获取kotlin方法的方法体（包含括号）
 */
val KtNamedFunction.body: KtBlockExpression?
  get() {
    return this.getChildOfType()
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
 * 取第一个特定类型的子元素
 */
inline fun <reified T> PsiElement.isFirstChildOfType(): Boolean {
  return this.parent != null && this.parent.children.firstOrNull { it is T } == this
}

/**
 * 获取java psi元素的工厂类
 * @param element psi元素
 * @return
 */
fun getPsiElementFactory(element: PsiElement): PsiElementFactoryImpl {
  return PsiElementFactoryImpl(PsiManagerEx.getInstanceEx(element.project))
}

/**
 * 返回当前psi元素工厂类
 */
val PsiElement.psiFactory: PsiElementFactoryImpl
  get() {
    return PsiElementFactoryImpl(PsiManagerEx.getInstanceEx(this.project))
  }

/**
 * 获取kt元素的工厂类
 */
val KtElement.ktFactory: KtPsiFactory
  get() {
    return KtPsiFactory(this.project)
  }

/**
 * 获取空白元素
 */
val whiteSpace: PsiWhiteSpace
  get() {
    return KtPsiFactory(ProjectCoreUtil.theProject).createWhiteSpace() as PsiWhiteSpace
  }

/**
 * 获取空行元素
 */
val newLine: PsiWhiteSpace
  get() {
    return KtPsiFactory(ProjectCoreUtil.theProject).createNewLine() as PsiWhiteSpace
  }

/**
 * psi元素的深度
 */
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

/**
 * 是否是父元素的第一个子元素
 */
val PsiElement.isFirstChild: Boolean
  get() {
    return this.parent != null && this.parent.firstChild == this
  }

/**
 * 重命名元素
 * @param element 待重命名的元素
 */
fun renameElement(element: PsiElement,
                  caretOffset: Int = -1,
                  parentElement: PsiElement? = null,
                  indexInParent: Int = -1) {
  val context = DataManager.getInstance()
      .dataContextFromFocus.result
  val editor = context.getData(CommonDataKeys.EDITOR)!!
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
  handler.invoke(realElement.project, editor, realElement.containingFile, context)
}
//val VirtualDirectoryImpl.cachedPosterity: ArrayList<VirtualFile>
//  get() {
//    val list = ArrayList<VirtualFile>()
//    getCachedChildren(list, this)
//    return list
//  }

//val PsiElement.posterity: ArrayList<PsiElement>
//  get() {
//    val list = ArrayList<PsiElement>()
//    getChildren(list, this)
//    return list
//  }

//private fun getChildren(list: ArrayList<PsiElement>, psiElement: PsiElement) {
//  if (psiElement.children.isNotEmpty()) {
//    list.addAll(psiElement.children)
//    psiElement.children.forEach {
//      getChildren(list, it)
//    }
//  }
//}

//private fun getCachedChildren(list: ArrayList<VirtualFile>, virtualDirectoryImpl: VirtualDirectoryImpl) {
//  list.addAll(virtualDirectoryImpl.cachedChildren.filter { !it.isDirectory })
//  virtualDirectoryImpl.cachedChildren.filter { it is VirtualDirectoryImpl }
//    .forEach {
//      getCachedChildren(list, it as VirtualDirectoryImpl)
//    }
//}


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
 * 获取指定类型的所有的祖先元素
 */
inline fun <reified T> PsiElement.getAncestorsOfType(): ArrayList<T> {
  val result = arrayListOf<T>()
  var el: PsiElement? = this.parent
  while (el != null) {
    if (el is T) {
      result.add(el)
    }
    el = el.parent
  }
  return result
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
 * 选择符合条件的第一个子元素
 * @param predicate 筛选条件
 * @return 符合条件的第一个子元素
 */
fun PsiElement.firstChild(predicate: (PsiElement) -> Boolean): PsiElement? {
  return this.children.firstOrNull(predicate)
}

/**
 * 在当前psi元素前插入元素
 * @param element 待插入的元素
 */
fun PsiElement.insertElementBefore(element: PsiElement): PsiElement {
  return this.parent.addBefore(element, this)
}

/**
 * 在当前psi元素后插入元素
 * @param element 待插入的元素
 */
fun PsiElement.insertElementAfter(element: PsiElement): PsiElement {
  return this.parent.addAfter(element, this)
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

