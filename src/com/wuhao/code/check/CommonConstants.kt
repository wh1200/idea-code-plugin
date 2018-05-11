/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.PsiElementFactoryImpl
import com.intellij.psi.impl.PsiManagerEx
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import java.util.logging.Logger


/**
 * junit的Test注解的类名称
 */
const val JUNIT_TEST_ANNOTATION_CLASS_NAME = "org.junit.Test"
/**
 * 默认的vue项目模板git地址
 */
const val DEFAULT_VUE_TEMPLATE_URL = "http://git2.aegis-info.com/template/aegis-vue-template/repository/archive.zip?ref=master"

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
 * 获取ktolin方法的方法体（包含括号）
 */
val KtNamedFunction.body: KtBlockExpression?
  get() {
    return this.getChildOfType<KtBlockExpression>()
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
 * 是否是父元素的第一个子元素
 */
val PsiElement.isFirstChild: Boolean
  get() {
    return this.parent != null && this.parent.firstChild == this
  }

/**
 * 注册元素错误提示信息
 * @param element 提示错误的元素
 * @param message 注册错误提示
 */
fun ProblemsHolder.registerError(elemenet: PsiElement, message: String) {
  this.registerProblem(elemenet, message, ProblemHighlightType.ERROR)
}

/**
 * 注册元素错误提示信息，并提供修复方法
 * @param elemenet 错误元素
 * @param message 错误提示信息
 * @param fix 修复方法
 */
fun ProblemsHolder.registerError(elemenet: PsiElement, message: String, fix: LocalQuickFix) {
  this.registerProblem(elemenet, message, ProblemHighlightType.ERROR, fix)
}

/**
 *
 */
inline fun <reified T> PsiElement.isFirstChildOfType(): Boolean {
  return this.parent != null && this.parent.children.firstOrNull { it is T } == this
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
 * 源码及资源文件基本路径
 */
private const val BASE_PATH = "src/main"

/**
 * 资源文件路径
 */
const val RESOURCES_PATH = "$BASE_PATH/resources"
/**
 * 源码路径
 */
//const val SOURCE_PATH = "$BASE_PATH/java"

/**
 * mybatis的mapper文件存放路径
 */
const val MAPPER_RELATIVE_PATH = "$RESOURCES_PATH/mapper"
/**
 * 默认缩进空格数
 */
const val DEFAULT_INDENT_SPACE_COUNT = 2
/**
 * 默认持续缩进空格数
 */
const val DEFAULT_CONTINUATION_INDENT_SPACE_COUNT = DEFAULT_INDENT_SPACE_COUNT * 2

/**
 * 获取java psi元素的工厂类
 * @param element psi元素
 * @return
 */
fun getPsiElementFactory(element: PsiElement): PsiElementFactoryImpl {
  return PsiElementFactoryImpl(PsiManagerEx.getInstanceEx(element.project))
}

/**
 * 全局日志
 */
val LOG: Logger = Logger.getLogger("plugin")

/**
 * kotlin的修饰符
 */
val KOTLIN_MODIFIERS = listOf(KtTokens.PROTECTED_KEYWORD,
    KtTokens.PRIVATE_KEYWORD, KtTokens.OPEN_KEYWORD)

