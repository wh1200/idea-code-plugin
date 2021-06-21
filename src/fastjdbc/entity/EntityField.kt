package fastjdbc.entity

import java.lang.reflect.Field
import java.lang.reflect.Method

class EntityField : Comparable<EntityField> {

  var columnName: String? = null
  var field: Field? = null
  /**  获取实体字段名称 */
  val fieldName: String?
    get() = this.field?.getName()
  /**  获取实体类型 */
  val javaType: Class<*>?
    get() = this.field?.getType()
  var order = 0
  var readMethod: Method? = null
  var writeMethod: Method? = null

  override fun compareTo(other: EntityField): Int {
    return order.compareTo(other.order)
  }

}
