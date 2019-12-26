package com.wuhao

import com.wuhao.code.check.constants.Messages.CLASS_COMMENT_REQUIRED
import com.wuhao.code.check.inspection.inspections.JavaCommentInspection

/**
 *
 * Created by 吴昊 on 2019/12/26.
 *
 * @author 吴昊
 * @since
 */
class CommentInspectionTest : BaseLightInspectionTest() {

  fun testJavaInspection() {
    val problems = doTestGlobalInspection("src/error/java/comment",
        JavaCommentInspection())
    problems.assertProblem("MissingClassComment.java", CLASS_COMMENT_REQUIRED)
  }

}
