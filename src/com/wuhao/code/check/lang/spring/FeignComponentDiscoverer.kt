package com.wuhao.code.check.lang.spring

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.spring.contexts.model.LocalModel
import com.intellij.spring.model.CommonSpringBean
import com.intellij.spring.model.custom.CustomLocalComponentsDiscoverer
import com.intellij.spring.model.jam.stereotype.SpringRepository
import com.wuhao.code.check.constants.Annotations.FEIGN_CLIENT
import com.wuhao.code.check.constants.Annotations.FEIGN_CLIENT_CONFIGURATION
import com.wuhao.code.check.constants.Annotations.IBATIS_MAPPER
import com.wuhao.code.check.constants.Annotations.MYBATIS_CONFIGURATION

/**
 * Created by 吴昊 on 2019/2/15.
 */
class FeignComponentDiscoverer : CustomLocalComponentsDiscoverer() {

  companion object {
    private val configurationClasses = mapOf(FEIGN_CLIENT_CONFIGURATION to FEIGN_CLIENT,
        MYBATIS_CONFIGURATION to IBATIS_MAPPER)
  }

  override fun getCustomComponents(springModel: LocalModel<*>): MutableCollection<CommonSpringBean> {
    val config = springModel.config
    if (config != null && config is PsiClass && config.qualifiedName in configurationClasses) {
      val project = springModel.config.project
      val facade = JavaPsiFacade.getInstance(project)
      val annotationClass = facade.findClass(configurationClasses[config.qualifiedName]!!,
          GlobalSearchScope.allScope(project))
      if (annotationClass != null) {
        return findAnnotatedBeanClasses(annotationClass).map {
          SpringRepository(it)
        }.toMutableList()
      }
    }
    return arrayListOf()
  }

  private fun findAnnotatedBeanClasses(annotationClass: PsiClass): List<PsiClass> {
    val annotatedClasses = AnnotatedElementsSearch.searchPsiClasses(
        annotationClass, GlobalSearchScope.allScope(annotationClass.project)
    )
    val beanClasses = annotatedClasses.filter { !it.isAnnotationType }
    val annotations = annotatedClasses.filter {
      it.isAnnotationType
    }
    val finalResult = beanClasses.toMutableList()
    if (annotations.isNotEmpty()) {
      annotations.forEach {
        finalResult.addAll(findAnnotatedBeanClasses(it))
      }
    }
    return finalResult.toList()
  }

}
