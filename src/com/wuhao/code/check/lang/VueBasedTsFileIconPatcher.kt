package com.wuhao.code.check.lang

import com.intellij.ide.FileIconPatcher
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.wuhao.code.check.hasDecorator
import com.wuhao.code.check.toPsiFile
import icons.VuejsIcons
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import javax.swing.Icon

/**
 * Created by 吴昊 on 2019/2/13.
 */
class VueBasedTsFileIconPatcher : FileIconPatcher {

  override fun patchIcon(icon: Icon, file: VirtualFile, p2: Int, project: Project?): Icon {
    if (project != null && file.name.endsWith(".ts")) {
      val tsFile = file.toPsiFile(project) as JSFile
      val exportDefault = tsFile.getChildOfType<ES6ExportDefaultAssignment>()
      if (exportDefault != null) {
        val attrList = exportDefault.getChildOfType<JSAttributeList>()
        if (attrList != null && attrList.hasDecorator("Component")) {
          return VuejsIcons.Vue
        }
      }
    }
    return icon
  }

}
