/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.PsiElementFactoryImpl
import com.intellij.psi.impl.PsiManagerEx
import org.jetbrains.kotlin.lexer.KtTokens
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

