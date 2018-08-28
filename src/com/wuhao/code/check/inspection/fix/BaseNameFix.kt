package com.wuhao.code.check.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.wuhao.code.check.*
import com.wuhao.code.check.enums.NamingMethod

/**
 * 元素命名修复基类
 * @author 吴昊
 * @since 1.3.7
 */
abstract class BaseNameFix(private val namingMethod: NamingMethod) : LocalQuickFix {

  override fun getFamilyName(): String {
    return "使用${namingMethod.zhName}命名法重命名"
  }

  protected fun getNewName(name: String): String {
    return when (namingMethod) {
      NamingMethod.Camel -> name.toCamelCase()
      NamingMethod.Pascal -> name.toPascalCase()
      NamingMethod.Constant -> name.toConstantCase()
      NamingMethod.Dash -> name.toDashCase()
      NamingMethod.Underline -> name.toUnderlineCase()
    }
  }

}

