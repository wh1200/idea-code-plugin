package com.wuhao

import com.wuhao.code.check.http.HttpRequest
import com.wuhao.code.check.http.HttpResult
import org.junit.Test


/**
 *
 * Created by  on 2018/7/4.
 *
 * @author
 * @since
 */
class CreateProjectTest {

  @Test
  fun test() {
    val templateUrl = "http://git2.aegis-info.com/template/aegis-vue-template/repository/archive.zip?ref=master"
    val gitPrivateToken = "yx-w-Fr3bfRyD1RC3XQD"
    val httpResult: HttpResult = HttpRequest.newGet(templateUrl)
        .withHeader("Private-Token", gitPrivateToken).execute()
    println(httpResult.bytes)
    println(httpResult.response)
  }

}

