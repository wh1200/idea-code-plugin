package fastjdbc

import java.sql.PreparedStatement

class Sql(
    var sql: String? = null,
    var args: List<Any>? = null
) {

  fun outputArgs(preStat: PreparedStatement) {
    var index = 1
    for (arg in args!!) {
      preStat.setObject(index++, arg)
    }
  }

}
