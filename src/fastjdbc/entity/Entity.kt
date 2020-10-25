package fastjdbc.entity

import java.util.*

class Entity {
  var beanClass: Class<*>? = null

  /**
   * 创建日期字段
   */
  var createDateField: EntityField? = null

  /**
   * 主键字段
   *
   * @see javax.persistence.Id
   */
  var idField: EntityField? = null

  /**
   * 不包含主键字段的其它字段，包括乐观锁子弹，创建日期，更新日期字段
   */
  var nonePrimaryKeyColumnMap: Map<String, EntityField>? = null

  /**
   * 不包含主键字段的其它字段，包括乐观锁版本字段，创建日期，更新日期字段
   */
  var nonePrimaryKeyFieldMap: Map<String, EntityField>? = null

  /**
   * 不包含主键字段的其它有序字段，包括乐观锁版本字段，创建日期，更新日期字段
   */
  var nonePrimaryKeyOrderedFields: Set<EntityField>? = null
  var tableName: String? = null

  /**
   * 更新日期字段
   */
  var updateDateField: EntityField? = null

  /**
   * 乐观锁版本字段
   *
   * @see javax.persistence.Version
   */
  var versionField: EntityField? = null

  fun getOptionalCreateDateField(): Optional<EntityField>? {
    return Optional.ofNullable(createDateField)
  }

  fun getOptionalUpdateDateField(): Optional<EntityField>? {
    return Optional.ofNullable(updateDateField)
  }

  fun getOptionalVersionField(): Optional<EntityField>? {
    return Optional.ofNullable(versionField)
  }

}
