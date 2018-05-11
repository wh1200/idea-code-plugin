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

  val BIND = "v-bind"
  val ELSE = "v-else"
  val FOR = "v-for"
  val HTML = "v-html"
  val IF = "v-if"
  val MODEL = "v-model"
  val ON = "v-on"
  val TEXT = "v-text"
  val VELSEIF = "v-elseif"

  fun getShortName(name: String): String {
    return when {
      name.startsWith(BIND) -> name.replaceFirst(BIND, "")
      name.startsWith(ON) -> name.replaceFirst("$ON:", "@")
      else -> name
    }
  }

}

