/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.constants

/**
 * 提示消息常量类
 * @author 吴昊
 * @since 1.3.0
 */
object Messages {

  const val REQUIRE_AUTHOR = "缺少作者信息"
  const val REQUIRE_VERSION = "缺少版本信息"
  const val CLASS_COMMENT_REQUIRED = "缺少类注释"
  const val COMMENT_REQUIRED = "缺少注释"
  const val COMPANION_CLASS_TO_OBJECT = "只包含伴随对象的类应该声明为object"
  const val CONVERT_TO_CLASS_COMPONENT = "转为class component"
  const val FIX_SPACE = "修复空格"
  const val FOR_SHORT = "应当进行缩写"
  const val FOR_TAG_SHOULD_HAVE_KEY_ATTR = "缺少:key属性"
  const val IF_AND_FOR_NOT_TOGETHER = "v-if 和 v-for 不能用在同个标签上"
  const val INTERFACE_METHOD_COMMENT_REQUIRED = "接口方法必须添加注释"
  const val JS_FILE_NAME_INVALID = "文件名称格式错误，只允许包含字母，数字，-及_"
  const val JUMP_TO_INTERFACE = "跳转至接口"
  const val MISSING_ATTR_VALUE = "缺少属性值"
  const val MISSING_COMMENT_CONTENT = "缺少注释内容"
  const val NAME_MUST_NOT_LESS_THAN2_CHARS = "名称不能少于2个字符"
  const val NO_CONSTANT_ARGUMENT = "不允许直接使用未经声明的常量作为参数"
  const val REDUNDANT_COMMENT = "多余的注释"
  const val VUE_COMPONENT_MISSING_NAME = "vue组件必须设置name"
  const val PARAMETER_COMMENT_MISSING = "参数说明不完全"
  const val DESCRIPTION_COMMENT_MISSING = "注释缺少描述"

}

