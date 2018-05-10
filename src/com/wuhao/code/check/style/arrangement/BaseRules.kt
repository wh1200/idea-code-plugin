/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.style.arrangement

/**
 * 排序规则列表类
 * @author 吴昊
 * @since 1.3.1
 */
abstract class BaseRules {
  abstract fun get(): List<RuleDescription>
}
