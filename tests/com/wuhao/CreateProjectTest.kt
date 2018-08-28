package com.wuhao

import com.wuhao.code.check.http.HttpRequest
import com.wuhao.code.check.http.HttpResult
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream


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
    unzip(httpResult.bytes!!, File(""))
    println(httpResult.response)
  }

  @Throws(Exception::class)
  private fun unzip(bytes: ByteArray, unzipFileDir: File) {
    try {
      if (!unzipFileDir.exists() || !unzipFileDir.isDirectory) {
        unzipFileDir.mkdirs()
      }
      val zip = ZipInputStream(ByteArrayInputStream(bytes))
      var entry = zip.nextEntry
      while (entry != null) {
        val entryFilePath = unzipFileDir.absolutePath + File.separator + if (!entry.name.contains("/")) {
          entry.name
        } else {
          entry.name.split("/").drop(n = 1).joinToString(File.separator)
        }
        println("1 " + entry.name + " ${entry.isDirectory}")
        println("2 " + entryFilePath)
        val entryFile = File(entryFilePath)
        if (entry.isDirectory) {
//          entryFile.mkdirs()
        } else {
//          entryFile.writeBytes(zip.readBytes())
        }
        entry = zip.nextEntry
      }
    } catch (e: Exception) {
      throw  e
    }
  }

}

