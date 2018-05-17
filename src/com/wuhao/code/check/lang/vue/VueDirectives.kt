/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.lang.vue

/**
 * vue指令
 * @author 吴昊
 * @since 1.3.1
 */
object VueDirectives {

  const val BIND = "v-bind"
  const val ELSE = "v-else"
  const val ELSE_IF = "v-elseif"
  const val FOR = "v-for"
  const val HTML = "v-html"
  const val IF = "v-if"
  const val MODEL = "v-model"
  const val ON = "v-on"
  const val TEXT = "v-text"

  fun getShortName(name: String): String {
    return when {
      name.startsWith(BIND) -> name.replaceFirst(BIND, "")
      name.startsWith(ON) -> name.replaceFirst("$ON:", "@")
      else -> name
    }
  }

}

