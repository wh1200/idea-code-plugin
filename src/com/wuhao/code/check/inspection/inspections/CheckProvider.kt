/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.inspections

import com.intellij.codeInspection.InspectionToolProvider
import java.io.File
import java.net.JarURLConnection

/**
 * @author 吴昊
 * @since 1.0
 */
class CheckProvider : InspectionToolProvider {

  override fun getInspectionClasses(): Array<Class<*>> {
    val packageName = this.javaClass.name.replace("." + this.javaClass.simpleName, "")
    val url = this.javaClass.getResource("/" + packageName.replace(".", "/"))
    val classes = arrayListOf<Class<*>>()
    if (url.protocol == "jar") {
      val jar = (url.openConnection() as JarURLConnection).jarFile
      val entries = jar.entries().toList()
      entries.forEach { entry ->
        var name = entry.name
        if (name[0] == '/') {
          name = name.substring(1)
        }
        name = name.replace("/", ".")
        if (name.startsWith(packageName) && name.endsWith(".class")) {
          val className = name.substring(packageName.length + 1, name.length - 6)
          classes.add(Class.forName("$packageName.$className"))
        }
      }
    } else if (url.protocol == "file") {
      val currentDirectory = File(url.file)
      val classFiles = currentDirectory.listFiles { _, name -> name.endsWith(".class") }
      classes.addAll(classFiles.map {
        Class.forName(packageName + "." + it.name.substring(0, it.name.indexOf(".")))
      })
    }
    return classes.filter { it.superclass == BaseInspection::class.java }.toTypedArray()
  }

}

