/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao

import com.google.gson.JsonParser
import java.io.File

/**
 * Created by 吴昊 on 18-4-25.
 */
fun main(args: Array<String>) {

  val packageJsonFile = File("/media/wuhao/新加卷/workspace/aegis/VueProject" + File.separator + "package.json")
  val el = JsonParser().parse(packageJsonFile.readText())
  el.asJsonObject.addProperty("name", "VueProject")
}
