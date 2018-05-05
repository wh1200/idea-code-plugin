/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.wuhao.code.check.inspection.visitor.*

/**
 * Created by 吴昊 on 18-4-26.
 */
open class CodeFormatVisitor(holder: ProblemsHolder) : PsiElementVisitor() {

  private val javaCodeFormatVisitor = JavaCodeFormatVisitor(holder)
  private val kotlinCodeFormatVisitor = KotlinCodeFormatVisitor(holder)
  private val vueCodeFormatVisitor = VueCodeFormatVisitor(holder)
  private val typescriptCodeFormatVisitor = TypeScriptCodeFormatVisitor(holder)
  private val javascriptCodeFormatVisitor = JavaScriptCodeFormatVisitor(holder)

  override fun visitElement(element: PsiElement) {
    super.visitElement(element)
    when (element.language.displayName) {
      JAVASCRIPT_LANGUAGE -> javascriptCodeFormatVisitor.visitElement(element)
      JAVASCRIPT_ECMA_6_LANGUAGE -> javascriptCodeFormatVisitor.visitElement(element)
      TYPESCRIPT_LANGUAGE -> typescriptCodeFormatVisitor.visitElement(element)
      VUE_LANGUAGE -> vueCodeFormatVisitor.visitElement(element)
      JAVA_LANGUAGE -> javaCodeFormatVisitor.visitElement(element)
      KOTLIN_LANGUAGE -> kotlinCodeFormatVisitor.visitElement(element)
    }
  }

  companion object {
    const val VUE_LANGUAGE = "Vue"
    const val JAVASCRIPT_LANGUAGE = "JavaScript"
    const val JAVASCRIPT_ECMA_6_LANGUAGE = "ECMAScript 6"
    const val TYPESCRIPT_LANGUAGE = "TypeScript"
    const val JAVA_LANGUAGE = "Java"
    const val KOTLIN_LANGUAGE = "Kotlin"
  }
}
