/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check

import com.intellij.psi.PsiElement

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
 * 按距离获取祖先元素，0为parent，如果没有找到则返回null
 * @param level 距离
 */
fun PsiElement.getAncestor(level: Int): PsiElement? {
  var el: PsiElement? = this
  for (i in 0..level) {
    el = el?.parent
    if (el == null) {
      return null
    }
  }
  return el
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



