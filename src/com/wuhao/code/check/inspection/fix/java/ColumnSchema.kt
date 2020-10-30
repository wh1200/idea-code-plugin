package com.wuhao.code.check.inspection.fix.java

import java.io.Serializable

class ColumnSchema : Serializable {

  var columnComment: String? = null
  var columnDefault: String? = null
  var columnKey: String? = null
  var columnName: String? = null
  var columnType: String? = null
  var dataType: String? = null
  var extra: String? = null
  var isNullable: String? = null
  var ordinalPosition = 0
  var tableName: String? = null
  var tableSchema: String? = null

  companion object {
    private const val serialVersionUID = -7523969607822355567L
  }

}
