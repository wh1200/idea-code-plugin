package com.wuhao.code.check.inspection.fix.vue

import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.core.replaced

/**
 *
 * @author 吴昊
 * @since 0.1.15
 */
class VueMethodRecursiveVisitor(private val propsNames: List<String>,
                                val refValueNames: List<String> = listOf(),
                                val computedNames: List<String> = listOf()) : JSElementVisitor() {

  val nameMap = mapOf(
      "this.${'$'}nextTick" to "nextTick",
      "this.${'$'}emit" to "emit",
      "this.${'$'}props" to ConvertToVue3Component.PROPS_PARAMETER_NAME
  )

  override fun visitElement(element: PsiElement) {
    super.visitElement(element)
    if (element is JSReferenceExpression) {
      if (element.text in nameMap) {
        val newExp = JSPsiElementFactory.createJSExpression(nameMap[element.text]!!, element)
        element.replaced(newExp)
        return
      }
      if (element.text.startsWith("this.")) {
        val name = element.text.replace("this.", "")
        if (name in propsNames) {
          element.replaced(JSPsiElementFactory.createJSExpression(
              element.text.replace(
                  "this.",
                  "${ConvertToVue3Component.PROPS_PARAMETER_NAME}."
              ), element
          ))
        } else if (name in computedNames || name in refValueNames) {
          element.replaced(JSPsiElementFactory.createJSExpression(
              element.text.replace(
                  "this.$name",
                  "$name.value"
              ), element
          ))
        }
        return
      }
    }
    element.children.forEach {
      it.accept(this)
    }
  }

}
