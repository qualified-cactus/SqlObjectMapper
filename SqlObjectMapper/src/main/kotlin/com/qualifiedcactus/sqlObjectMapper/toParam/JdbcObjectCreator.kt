package com.qualifiedcactus.sqlObjectMapper.toParam

import java.sql.*

/**
 * A class used to create JDBC specific's data object
 */
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