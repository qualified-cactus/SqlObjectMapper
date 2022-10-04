import java.sql.*

typealias PropertyAccessor = (o: Any) -> Any?
typealias PropertySetter = (o: Any, value: Any?)-> Unit
typealias ValueProvider = (colName: String) -> Any?;


interface OneToManyMapping {
    val elemClassMapping: ClassMapping<*>
    fun addToCollection(parent: Any, obj: Any?)
}

interface ClassMapping<T : Any> {
    val idColumn: String
    val oneToManyMappings: List<OneToManyMapping>

    fun createObject(colNameToValue: ValueProvider): T
    fun getColumnNameValueMap(jdbcObjectCreator: JdbcObjectCreator, o: T): Map<String, Any?>
}



class JdbcObjectCreator(private val conn: Connection) {

    fun createArrayOf(typeName: String?, elements: Array<Any?>?): java.sql.Array? {
        return conn.createArrayOf(typeName, elements)
    }

    fun createBlob(): Blob? {
        return conn.createBlob()
    }

    fun createNClob(): NClob? {
        return conn.createNClob()
    }

    fun createSQLXML(): SQLXML? {
        return conn.createSQLXML()
    }

    fun createStruct(typeName: String?, attributes: Array<Any?>?): Struct? {
        return conn.createStruct(typeName, attributes)
    }
}

