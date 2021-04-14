/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix.java

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.*import com.intellij.psi.impl.PsiElementFactoryImpl
import com.wuhao.code.check.ui.PluginSettings
import java.text.SimpleDateFormat
import java.util.*


/**
 * java元素添加块注释修复
 * Created by 吴昊 on 18-4-26.
 * @author 吴昊
 * @since 1.1
 */
class JavaBlockCommentFix : LocalQuickFix {

  companion object {
    const val BLOCK_COMMENT_END = "*/"
    const val BLOCK_COMMENT_START = "/**"
    const val BLOCK_COMMENT_STRING = "$BLOCK_COMMENT_START $BLOCK_COMMENT_END"
    val CLASS_COMMENT = """$BLOCK_COMMENT_START
* @author ${PluginSettings.NULLABLE_INSTANCE?.user}
* @date ${Date().format("yyyy/MM/dd")}
* @version 1.0 
* @since 
$BLOCK_COMMENT_END"""
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val element = descriptor.psiElement
    val measureElement = if (element is PsiIdentifier) {
      element.parent
    } else {
      element
    }
    val commentElement = buildComment(measureElement, element)
    if (element is PsiIdentifier) {
      element.parent.addBefore(commentElement, element.parent.firstChild)
    } else {
      element.addBefore(commentElement, element.firstChild)
    }
  }

  private fun buildComment(measureElement: PsiElement, element: PsiElement): PsiElement {
    val commentText = when (measureElement) {
      is PsiClass -> CLASS_COMMENT
      is PsiMethod -> buildMethodComment(measureElement)
      else -> BLOCK_COMMENT_STRING
    }
    val factory = PsiElementFactoryImpl(element.project)
    return factory.createCommentFromText(commentText, element)
  }

  override fun getFamilyName(): String {
    return "添加注释"
  }

  private fun buildMethodComment(element: PsiMethod): String {
    val commentBuilder = StringBuilder("$BLOCK_COMMENT_START\n")
    element.parameterList.parameters.forEach {
      commentBuilder.append("* @param ${it.name}\n")
    }
    if (element.returnType != PsiPrimitiveType.VOID) {
      commentBuilder.append("* @return \n")
    }
    commentBuilder.append(BLOCK_COMMENT_END)
    return commentBuilder.toString()
  }

}

/**
 * 格式化日期
 * @param pattern 格式
 * @return 格式化的日期字符串
 */
private fun Date.format(pattern: String): String {
  return SimpleDateFormat(pattern).format(this)
}

