/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.lang.Language

/**
 * 代码格式检查访问器基础接口
 * @author 吴昊
 * @since 1.3.2
 */
interface BaseCodeFormatVisitor {

  /**
   * 访问器是否支持某种语言
   * @param language 所要检查的语言
   * @return 如果支持该语言返回true，不支持返回false
   */
  fun support(language: Language): Boolean

}

