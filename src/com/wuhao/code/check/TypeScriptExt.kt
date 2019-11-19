package com.wuhao.code.check

import com.intellij.lang.javascript.psi.JSField
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClassExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass

/**
 *
 * Created by 吴昊 on 2019/2/12.
 *
 * @author 吴昊
 * @since 1.4.6
 */
/**  获取ts类所有的字段（包含继承的字段） */
val TypeScriptClassExpression.allFields: List<JSField>
  get() {
    return this.getAllClasses().map { it.fields.toList() }.flatten()
  }

/**  获取ts类所有的方法（包含继承的方法） */
val TypeScriptClassExpression.allFunctions: List<JSFunction>
  get() {
    return this.getAllClasses().map { it.functions.toList() }.flatten()
  }

/**
 * 获取包含自己以及所有继承的类的列表
 * @return
 */
private fun TypeScriptClassExpression.getAllClasses(): ArrayList<JSClass> {
  val classList: ArrayList<JSClass> = arrayListOf(this)
  if (this.extendsList != null) {
    this.extendsList!!.referencedClasses.forEach {
      if (!classList.contains(it)) {
        classList.add(it)
      }
    }
  }
  return classList
}
