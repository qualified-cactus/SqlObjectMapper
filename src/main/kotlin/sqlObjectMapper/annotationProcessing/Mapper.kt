/*
 MIT License

 Copyright (c) 2022 qualified-cactus

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package sqlObjectMapper.annotationProcessing


import java.sql.*

typealias ValueProvider = (colName: String) -> Any?

interface OneToManyMapping {
    val elemClassMapping: ClassMapping<*>
    fun addToCollection(parent: Any, obj: Any?)
}

/**
 * A class mapping of a class contains the following information:
 * - how an object of that class can be created.
 * - how to get its properties and its related relational information.
 */
interface ClassMapping<T> {
    val clazz: Class<T>
    val idMapping: IdMapping
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
