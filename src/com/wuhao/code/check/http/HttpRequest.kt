/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
@file:Suppress("unused")

package com.wuhao.code.check.http

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethodBase
import org.apache.commons.httpclient.NameValuePair
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.methods.*
import org.apache.commons.httpclient.methods.multipart.FilePart
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity
import org.apache.commons.httpclient.methods.multipart.Part
import org.apache.commons.httpclient.methods.multipart.StringPart
import org.apache.commons.httpclient.params.HttpMethodParams
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import java.io.*
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Created by wuhao on 2016/6/27.
 */
class HttpRequest private constructor() {

  private var body: String? = null
  private var encoding: String? = null
  private val headers: MutableMap<String, String>?
  private var ip: String? = null
  private var listener: RequestProgressListener? = null
  private var method: Method? = null
  private val params: MutableMap<String, Any>?
  private var password: String? = null
  private var port: Int = 0
  private var timeout: Int = 0
  private var url: String? = null
  private var username: String? = null

  companion object {
    const val BUFFER_SIZE = 4096

    var log: Log = LogFactory.getLog(HttpRequest::class.java)

    fun decodeUnicode(str: String): String {
      val set = Charset.forName("UTF-16")
      val p = Pattern.compile("\\\\u([0-9a-fA-F]{4})")
      val m = p.matcher(str)
      var start = 0
      var start2: Int
      val sb = StringBuilder()
      while (m.find(start)) {
        start2 = m.start()
        if (start2 > start) {
          val seg = str.substring(start, start2)
          sb.append(seg)
        }
        val code = m.group(1)
        val len = 16
        val i = Integer.valueOf(code, len)
        val bb = ByteArray(4)
        bb[0] = (i shr 8 and 0xFF).toByte()
        bb[1] = (i and 0xFF).toByte()
        val b = ByteBuffer.wrap(bb)
        sb.append(set.decode(b).toString().trim { it <= ' ' })
        start = m.end()
      }
      start2 = str.length
      if (start2 > start) {
        val seg = str.substring(start, start2)
        sb.append(seg)
      }
      return sb.toString()
    }

    fun newDelete(): HttpRequest {
      return HttpRequest().deleteMethod()
    }

    fun newDelete(url: String): HttpRequest {
      return HttpRequest().deleteMethod(url)
    }

    fun newGet(): HttpRequest {
      return HttpRequest().getMethod()
    }

    fun newGet(url: String): HttpRequest {
      return HttpRequest().getMethod(url)
    }

    fun newHead(): HttpRequest {
      return HttpRequest().headMethod()
    }

    fun newHead(url: String): HttpRequest {
      return HttpRequest().headMethod(url)
    }

    fun newInstance(): HttpRequest {
      return HttpRequest()
    }

    fun newPost(): HttpRequest {
      return HttpRequest().postMethod()
    }

    fun newPost(url: String): HttpRequest {
      return HttpRequest().postMethod(url)
    }

    fun newPut(): HttpRequest {
      return HttpRequest().putMethod()
    }

    fun newPut(url: String): HttpRequest {
      return HttpRequest().putMethod(url)
    }
  }

  init {
    this.params = HashMap()
    this.headers = HashMap()
    this.encoding = "UTF-8"
  }

  fun deleteMethod(): HttpRequest {
    return this.method(Method.Delete)
  }

  fun deleteMethod(url: String): HttpRequest {
    return this.method(Method.Delete).withUrl(url)
  }

  fun execute(): HttpResult {
    val request: RequestThread
    when (method) {
      HttpRequest.Method.Post -> request = Post()
      HttpRequest.Method.Delete -> request = Delete()
      HttpRequest.Method.Get -> request = Get()
      HttpRequest.Method.Head -> request = Head()
      HttpRequest.Method.Put -> request = Put()
      else -> {
        request = Get()
      }
    }
    try {
      request.start()
      request.join()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return request!!.result!!
  }

  fun getMethod(): HttpRequest {
    return this.method(Method.Get)
  }

  fun getMethod(url: String): HttpRequest {
    return this.method(Method.Get).withUrl(url)
  }

  fun headMethod(): HttpRequest {
    return this.method(Method.Head)
  }

  fun headMethod(url: String): HttpRequest {
    return this.method(Method.Head).withUrl(url)
  }

  fun postMethod(): HttpRequest {
    return this.method(Method.Post)
  }

  fun postMethod(url: String): HttpRequest {
    return this.method(Method.Post).withUrl(url)
  }

  fun putMethod(): HttpRequest {
    return this.method(Method.Put)
  }

  fun putMethod(url: String): HttpRequest {
    return this.method(Method.Put).withUrl(url)
  }

  fun withAuthentication(username: String, password: String): HttpRequest {
    this.username = username
    this.password = password
    return this
  }

  fun withBody(body: String): HttpRequest {
    this.body = body
    return this
  }

  fun withEncoding(encoding: String): HttpRequest {
    this.encoding = encoding
    return this
  }

  fun withHeader(key: String, value: String): HttpRequest {
    this.headers!![key] = value
    return this
  }

  fun withListener(listener: RequestProgressListener): HttpRequest {
    this.listener = listener
    return this
  }

  fun withParam(key: String, value: Any): HttpRequest {
    this.params!![key] = value
    return this
  }

  fun withParams(params: Map<String, Any>?): HttpRequest {
    if (params != null) {
      this.params!!.putAll(params)
    }
    return this
  }

  fun withProxy(ip: String, port: Int): HttpRequest {
    this.ip = ip
    this.port = port
    return this
  }

  fun withTimeout(timeout: Int): HttpRequest {
    this.timeout = timeout
    return this
  }

  private fun containsFile(): Boolean {
    for (key in params!!.keys) {
      if (params[key] != null && (params[key] is File || params[key] is Part)) {
        return true
      }
    }
    return false
  }

  private fun createClient(): HttpClient {
    val client: HttpClient = if (ip != null && port > 0) {
      getProxyClient(ip!!, port)
    } else {
      HttpClient()
    }
    if (timeout > 0) {
      client.httpConnectionManager.params.connectionTimeout = timeout
    }
    return client
  }

  private fun execute(client: HttpClient, method: HttpMethodBase): HttpResult {
    if (headers != null) {
      for (key in headers.keys) {
        method.addRequestHeader(key, headers[key])
      }
    }
    if (username != null && password != null) {
      client.state.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(username!!, password))
    }
    val result = HttpResult()
    try {
      val code = client.executeMethod(method)
      result.status = code
      result.setHeaders(method.responseHeaders)
      val stream = method.responseBodyAsStream
      if (stream != null) {
        val contentType = method.getResponseHeader("Content-Type")?.value
        val acceptRanges = method.getResponseHeader("Accept-Ranges")?.value
        val length = method.responseContentLength
        if (acceptRanges == "bytes" || contentType == "application/java-archive") {
          val bytes = readBytes(stream, length)
          result.bytes = bytes
          result.contentLength = length
        } else {
          val br = BufferedReader(InputStreamReader(
              stream, encoding!!))
          val resBuffer = StringBuilder()
          var resTemp = br.readLine()
          while (resTemp != null) {
            resBuffer.append(resTemp).append("\n")
            resTemp = br.readLine()
          }
          var response = resBuffer.toString().trim { it <= ' ' }
          response = decodeUnicode(response)
          result.response = response
        }

        stream.close()
      }
    } catch (e: Exception) {
      result.exception = e
    } finally {
      method.releaseConnection()
    }
    return result
  }

  private fun getProxyClient(proxyHost: String, proxyPort: Int): HttpClient {
    val httpClient = HttpClient()
    val username = ""
    val password = ""
    httpClient.hostConfiguration.setProxy(proxyHost, proxyPort)
    val credentials = UsernamePasswordCredentials(
        username, password)
    httpClient.state.setProxyCredentials(AuthScope.ANY, credentials)
    return httpClient
  }

  private fun method(method: Method): HttpRequest {
    this.method = method
    return this
  }

  @Throws(IOException::class)
  private fun readBytes(input: InputStream, length: Long): ByteArray {
    val output = ByteArrayOutputStream()
    var count: Long = 0
    val buffer = ByteArray(BUFFER_SIZE)
    if (listener != null) {
      listener!!.start(length)
    }
    var n = input.read(buffer)
    while (-1 != n) {
      output.write(buffer, 0, n)
      count += n.toLong()
      if (listener != null) {
        listener!!.inProgress(n.toLong(), count, length)
      }
      n = input.read(buffer)
    }
    if (listener != null) {
      listener!!.end()
    }
    return output.toByteArray()
  }

  private fun withUrl(url: String): HttpRequest {
    this.url = url
    return this
  }

  /**
   * delete 请求线程
   * @author 吴昊
   * @since 1.3.3
   */
  private inner class Delete internal constructor() : RequestThread() {

    override fun run() {
      log.info("http headMethod:" + url!!)
      val deleteMethod = DeleteMethod(url)
      deleteMethod.params.setParameter(
          HttpMethodParams.HTTP_CONTENT_CHARSET, encoding)
      result = execute(createClient(), deleteMethod)
    }

  }

  /**
   * get 请求线程
   * @author 吴昊
   * @since 1.3.3
   */
  private inner class Get : RequestThread() {

    override fun run() {
      if (params != null && params.isNotEmpty()) {
        val builder = StringBuilder()
        builder.append("?")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        for (key in params.keys) {
          val value = params[key]
          try {
            when {
              value is Date -> {
                if (builder.length > 1) {
                  builder.append("&")
                }
                builder.append(key).append("=").append(URLEncoder.encode(sdf.format(value), encoding!!))
              }
              value is Collection<*> -> for (v in value) {
                if (builder.length > 1) {
                  builder.append("&")
                }
                builder.append(key).append("=").append(URLEncoder.encode(v.toString(), encoding!!))
              }
              value!!.javaClass.isArray -> {
                val array = value as Array<*>
                for (v in array) {
                  if (builder.length > 1) {
                    builder.append("&")
                  }
                  builder.append(key).append("=").append(URLEncoder.encode(v.toString(), encoding!!))
                }
              }
              else -> {
                if (builder.length > 1) {
                  builder.append("&")
                }
                builder.append(key).append("=").append(URLEncoder.encode(value.toString(), encoding!!))
              }
            }
          } catch (e: Exception) {
            e.printStackTrace()
          }

        }
        url = url!! + builder.toString()
      }

      val getMethod = GetMethod(url)
      getMethod.params.setParameter(
          HttpMethodParams.HTTP_CONTENT_CHARSET, encoding)
      result = execute(createClient(), getMethod)
    }

  }

  /**
   * head 请求线程
   * @author 吴昊
   * @since 1.3.3
   */
  private inner class Head internal constructor() : RequestThread() {

    override fun run() {
      log.info("http headMethod:" + url!!)
      val headMethod = HeadMethod(url)
      headMethod.params.setParameter(
          HttpMethodParams.HTTP_CONTENT_CHARSET, encoding)
      result = execute(createClient(), headMethod)
    }

  }

  /**
   * post 请求线程
   * @author 吴昊
   * @since 1.3.3
   */
  private inner class Post internal constructor() : RequestThread() {

    override fun run() {
      log.debug("http postMethod:" + url!!)
      val postMethod = PostMethod(url)
      postMethod.params.setParameter(
          HttpMethodParams.HTTP_CONTENT_CHARSET, encoding)
      // 设置Post数据
      if (params != null && !params.isEmpty()) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val data = ArrayList<NameValuePair>()
        val parts = ArrayList<Part>()
        params.forEach { key, value ->
          when {
            value is Part -> parts.add(value)
            value is File -> try {
              parts.add(FilePart(key, value))
            } catch (e: FileNotFoundException) {
              e.printStackTrace()
            }
            value is Date -> data.add(NameValuePair(key, sdf.format(value)))
            value is Collection<*> -> for (v in value) {
              data.add(NameValuePair(key, v.toString()))
            }
            value.javaClass.isArray -> {
              val array = value as Array<*>
              for (v in array) {
                data.add(NameValuePair(key, v.toString()))
              }
            }
            else -> data.add(NameValuePair(key, value.toString()))
          }
        }
        if (containsFile()) {
          if (data.size > 0 || parts.size > 0) {
            val partArray = arrayOfNulls<Part>(data.size + parts.size)
            for (i in partArray.indices) {
              if (i < data.size) {
                partArray[i] = StringPart(data[i].name, data[i].value)
              } else {
                partArray[i] = parts[i - data.size]
              }
            }
            postMethod.requestEntity = MultipartRequestEntity(partArray, postMethod.params)
          }
        } else {
          if (data.size > 0) {
            val array = arrayOfNulls<NameValuePair>(data.size)
            for (i in data.indices) {
              array[i] = data[i]
            }
            postMethod.setRequestBody(array)
          }
        }
      }
      if (body != null) {
        try {
          postMethod.requestEntity = StringRequestEntity(body!!, "application/json", "UTF-8")
        } catch (e: UnsupportedEncodingException) {
          e.printStackTrace()
        }

      }
      result = execute(createClient(), postMethod)
    }

  }

  /**
   * put 请求线程
   * @author 吴昊
   * @since 1.3.3
   */
  private inner class Put internal constructor() : RequestThread() {

    override fun run() {
      log.debug("http putMethod:" + url!!)
      val putMethod = PutMethod(url)
      putMethod.params.setParameter(
          HttpMethodParams.HTTP_CONTENT_CHARSET, encoding)
      if (body != null) {
        try {
          putMethod.requestEntity = StringRequestEntity(body, "application/json", "UTF-8")
        } catch (e: UnsupportedEncodingException) {
          e.printStackTrace()
        }
      }
      result = execute(createClient(), putMethod)
    }

  }

  /**
   * http请求线程
   * @author 吴昊
   * @since 1.3.3
   */
  private open inner class RequestThread internal constructor() : Thread() {

    var result: HttpResult? = null

  }

  /**
   * http请求方法
   */
  enum class Method {

    Delete,
    Get,
    Head,
    Patch,
    Post,
    Put;

  }

}

