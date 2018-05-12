/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.http

import org.apache.commons.httpclient.Header
import java.util.*

/**
 * Created by wuhao on 2016/6/27.
 * http 请求结果
 */
class HttpResult {

  /**
   * 请求返回的二进制数据（下载文件的请求）
   */
  var bytes: ByteArray? = null
  /**
   * 请求返回的内容长度
   */
  var contentLength: Long = 0
  /**
   * 请求发生的异常
   */
  var exception: Exception? = null
  /**
   * http响应头
   */
  private var headers: MutableMap<String, String>? = null
  /**
   * http响应的内容
   */
  var response: String? = null
  /**
   * http请求的状态
   */
  var status: Int = 0

  override fun toString(): String {
    val sb = StringBuilder()
    sb.append(status)
    if (response != null) {
      sb.append(" : ")
      sb.append(response)
    } else if (exception != null) {
      sb.append(exception!!.message)
    }
    return sb.toString()
  }

  fun getHeaders(): Map<String, String>? {
    return headers
  }

  fun setHeaders(headers: Array<Header>) {
    this.headers = HashMap()
    Arrays.stream(headers).forEach { header -> this.headers!![header.name] = header.value }
  }
}
