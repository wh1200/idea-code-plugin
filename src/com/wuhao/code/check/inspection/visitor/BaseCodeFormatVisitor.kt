/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.wuhao.code.check.inspection.checker.ClassCommentChecker

/**
 * Created by 吴昊 on 18-4-26.
 */
abstract class BaseCodeFormatVisitor(protected val holder: ProblemsHolder) {

  protected val classCommentChecker = ClassCommentChecker(holder)

  abstract fun visitElement(element:PsiElement)
}
