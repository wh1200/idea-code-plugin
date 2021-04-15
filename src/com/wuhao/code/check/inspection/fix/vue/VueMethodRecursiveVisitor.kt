package com.wuhao.code.check.inspection.fix.vue

import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.xml.XmlAttribute
import org.jetbrains.kotlin.idea.core.replaced

/**
 *
 * @author 吴昊
 * @since 0.1.15
 */
class VueMethodRecursiveVisitor(
    private val propsNames: List<String>,
    private val refValueNames: List<String> = listOf(),
    private val methodNames: List<String> = listOf(),
    private val computedNames: List<String> = listOf(),
    private val reactiveValueNames: List<String> = listOf(),
    private val vueVersion: Int?,
    private val injectionNames: ArrayList<String>
) : JSElementVisitor() {

  val nameMap = mapOf(
      "this.${'$'}nextTick" to "nextTick",
      "this.${'$'}emit" to "emit",
      "this.${'$'}props" to Vue2ClassToVue3CompositionAPIFix.PROPS_PARAMETER_NAME
  )
  val refs = hashSetOf<String>()

  override fun visitElement(element: PsiElement) {
    super.visitElement(element)
    if (element is JSReferenceExpression) {
      if (element.text in nameMap) {
        val newExp = JSPsiElementFactory.createJSExpression(nameMap[element.text]!!, element)
        element.replaced(newExp)
      }
      if (element.text.startsWith("this.${'$'}refs.")) {
        val name = element.text.replace("this.${'$'}refs.", "")
        if (!name.contains(".")) {
          refs.add(name)
          element.replaced(
              JSPsiElementFactory.createJSExpression(
                  "${name}Ref.value", element
              )
          )
        }
      }
      if (element.text.startsWith("this.")) {
        val name = element.text.replace("this.", "")
        if (name in propsNames) {
          if (vueVersion == 2) {
            element.replaced(
                JSPsiElementFactory.createJSExpression(
                    "(" + element.text.replace(
                        "this.",
                        "${Vue2ClassToVue3CompositionAPIFix.PROPS_PARAMETER_NAME}."
                    ) + " as any)", element
                )
            )
          } else {
            element.replaced(
                JSPsiElementFactory.createJSExpression(
                    element.text.replace(
                        "this.",
                        "${Vue2ClassToVue3CompositionAPIFix.PROPS_PARAMETER_NAME}."
                    ), element
                )
            )
          }
        } else if (name in computedNames || name in refValueNames) {
          element.replaced(
              JSPsiElementFactory.createJSExpression(
                  element.text.replace(
                      "this.$name",
                      "$name.value"
                  ), element
              )
          )
        } else if (name in methodNames || name in reactiveValueNames || name in injectionNames) {
          element.replaced(
              JSPsiElementFactory.createJSExpression(
                  element.text.replace(
                      "this.",
                      ""
                  ), element
              )
          )
        } else if (name.startsWith("$") && vueVersion == 2) {
          element.replaced(
              JSPsiElementFactory.createJSExpression(
                  element.text.replace(
                      "this.",
                      "root."
                  ), element
              )
          )
        }
      }
    } else if (element is XmlAttribute) {
      if (element.name == "ref") {
        refs.add(element.value!!)
        element.replaced(
            XmlElementFactory.getInstance(element.project)
                .createXmlAttribute(
                    "ref", """{(el) => {
              | ${element.value}Ref.value = el;
              |}}""".trimMargin()
                )
        )
      }
    }
    element.children.forEach {
      it.accept(this)
    }
  }

}
