package `fun`.hydd.cdda_browser.model.userType

import io.vertx.core.json.JsonObject
import org.hibernate.HibernateException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types
import java.util.*

/**
 * copy from https://github.com/hibernate/hibernate-reactive/blob/main/hibernate-reactive-core/src/test/java/org/hibernate/reactive/types/Json.java
 *
 */
class Json : UserType {
  override fun sqlTypes(): IntArray {
    return intArrayOf(Types.OTHER)
  }

  override fun returnedClass(): Class<*> {
    return JsonObject::class.java
  }

  @Throws(HibernateException::class)
  override fun equals(x: Any, y: Any): Boolean {
    return Objects.equals(x, y)
  }

  @Throws(HibernateException::class)
  override fun hashCode(x: Any): Int {
    return Objects.hashCode(x)
  }

  @Throws(HibernateException::class, SQLException::class)
  override fun nullSafeGet(
    rs: ResultSet,
    names: Array<String>,
    session: SharedSessionContractImplementor,
    owner: Any
  ): Any {
    val rsObject = rs.getObject(names[0])
    // Currently Vertx does not return JsonObject type from MariaDb so adding type check to convert the String value
    return if (rsObject is String) {
      JsonObject(rsObject.toString())
    } else rsObject
  }

  @Throws(HibernateException::class, SQLException::class)
  override fun nullSafeSet(st: PreparedStatement, value: Any?, index: Int, session: SharedSessionContractImplementor) {
    if (value == null) {
      st.setNull(index, Types.OTHER)
    } else {
      st.setObject(index, value)
    }
  }

  @Throws(HibernateException::class)
  override fun deepCopy(value: Any?): Any {
    return (if (value == null) null else (value as JsonObject).copy())!!
  }

  override fun isMutable(): Boolean {
    return true
  }

  @Throws(HibernateException::class)
  override fun disassemble(value: Any): Serializable {
    throw UnsupportedOperationException()
  }

  @Throws(HibernateException::class)
  override fun assemble(cached: Serializable, owner: Any): Any {
    throw UnsupportedOperationException()
  }

  @Throws(HibernateException::class)
  override fun replace(original: Any, target: Any, owner: Any): Any {
    throw UnsupportedOperationException()
  }
}
