/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement

import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken

/**
 * java代码排序规则描述
 * @author 吴昊
 * @since 1.2.6
 */
class RuleDescription(val template: List<ArrangementSettingsToken>) {

  var order: ArrangementSettingsToken? = null

  constructor(template: List<ArrangementSettingsToken>, order: ArrangementSettingsToken)
      : this(template) {
    this.order = order
  }

  constructor(token: ArrangementSettingsToken) : this(listOf(token))
  constructor(token: ArrangementSettingsToken, order: ArrangementSettingsToken) : this(listOf(token), order)
}

