package com.wuhao.code.check

import com.intellij.openapi.project.Project
import org.jetbrains.idea.maven.project.MavenProjectsManager

/**
 *
 * @author 吴昊
 * @date 2020/10/19 13:53
 * @since TODO
 * @version 1.0
 */


/**
 * 读取项目版本号，目前支持maven
 */
fun Project.getVersion(): String? {
  val mavenProjectsManager = MavenProjectsManager.getInstance(this)
  if (mavenProjectsManager != null && mavenProjectsManager.hasProjects()) {
    val mavenProject = mavenProjectsManager.projects.firstOrNull()
    if (mavenProject != null) {
      return mavenProject.modelMap["version"]
    }
  }
  return null
}
