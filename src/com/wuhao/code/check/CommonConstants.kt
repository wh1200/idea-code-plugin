/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import java.util.logging.Logger


/**
 * 修改字段、属性或变量名称时的临时名称
 */
const val PROPERTY_NAME_PLACEHOLDER = "_tmp"

/**
 * 直接使用字符串作为参数时允许的最大长度
 */
const val MAX_STRING_ARGUMENT_LENGTH = 1000

/**
 * junit的Test注解的类名称
 */
const val JUNIT_TEST_ANNOTATION_CLASS_NAME = "org.junit.Test"
/**
 * 默认的vue项目模板git地址
 */
const val DEFAULT_VUE_TEMPLATE_URL = "http://git2.aegis-info.com/template/aegis-vue-template/repository/archive.zip?ref=master"

/**
 * 默认的java&kotlin项目模板git地址
 */
const val DEFAULT_JAVA_KOTLIN_TEMPLATE_URL = "http://git2.aegis-info.com/template/JavaKotlinTemplate/repository/archive.zip?ref=master"

/**
 * 注册元素错误提示信息
 * @param element 提示错误的元素
 * @param message 注册错误提示
 */
fun ProblemsHolder.registerError(element: PsiElement, message: String) {
  this.registerProblem(element, message, ProblemHighlightType.ERROR)
}

/**
 * 注册元素错误提示信息，并提供修复方法
 * @param element 错误元素
 * @param message 错误提示信息
 * @param fix 修复方法
 */
fun ProblemsHolder.registerError(element: PsiElement, message: String, fix: LocalQuickFix) {
  this.registerProblem(element, message, ProblemHighlightType.ERROR, fix)
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
 * 全局日志
 */
val LOG: Logger = Logger.getLogger("plugin")

/**
 * kotlin的修饰符
 */
val KOTLIN_MODIFIERS = listOf(KtTokens.PROTECTED_KEYWORD,
    KtTokens.PRIVATE_KEYWORD, KtTokens.OPEN_KEYWORD)

