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

package sqlObjectMapper

import sqlObjectMapper.NpPreparedStatement.Companion.prepareNpStatement
import java.sql.Connection
import java.sql.ResultSet

/**
 * A convenient wrapper to simply the interactions
 * among [ClassMappingProvider] [NpPreparedStatement],
 * [NpCallableStatement] and [MappedResultSet].
 */
class QueryExecutor(
    val mappingProvider: ClassMappingProvider
) {
    
    /**
     * Execute a prepared statement using a sql string with named parameters.
     * The statement is closed after this method returns.
     * @param connection a Jdbc Connection
     * @param sql a sql string with named parameters
     * @param parametersDto a data object to set the parameters, or null if the statement doesn't have any parameters
     * @param processStatement a function that consume an already executed statement
     * and a class mapping provider to return a value
     * @return value returned by param [processStatement]
     */
    private fun <T> executeQuery(
        connection: Connection,
        sql: String,
        parametersDto: Any?,
        processStatement: (mappedRs: MappedResultSet) -> T
    ): T {
        if (parametersDto == null) {
            return connection.createStatement().use { stmt ->
                processStatement(MappedResultSet(stmt.executeQuery(sql), mappingProvider))
            }
        }
        else {
            return connection.prepareNpStatement(sql).use { stmt ->
                stmt.setParameters(parametersDto, mappingProvider)
                stmt.execute()
                processStatement(MappedResultSet(stmt.resultSet, mappingProvider))
            }
        }
    }

    /**
     * Execute a prepared statement and get the first row as an object.
     * The statement is closed after this method returns.
     * @param connection a Jdbc Connection
     * @param sql a sql string with named parameters
     * @param parametersDto a data object to set the parameters,
     * or null if the statement doesn't have any parameters
     * @param resultType type of the returned result.
     * @return an object get from the first row of the [ResultSet] of this statement,
     * or null of there is no row.
     */
    fun <T : Any> queryForObject(
        connection: Connection,
        sql: String,
        parametersDto: Any?,
        resultType: Class<T>
    ): T? {
        return executeQuery(connection, sql, parametersDto) { rs ->
            rs.toObject(resultType)
        }
    }

    /**
     * Execute a prepared statement and parse the rows of [ResultSet] into a list of objects.
     * The statement is closed after this method returns.
     * @param connection a Jdbc Connection
     * @param sql a sql string with named parameters
     * @param parametersDto a data object to set the parameters,
     * or null if the statement doesn't have any parameters
     * @param elementType the element type of the returned collection
     * @return a collection of objects parsed from [ResultSet]
     */
    fun <T : Any> queryForList(
        connection: Connection,
        sql: String,
        parametersDto: Any?,
        elementType: Class<T>
    ): List<T> {
        return executeQuery(connection, sql, parametersDto) { rs ->
            rs.toList(elementType)
        }
    }

    /**
     * Get value of the first column of the first row of [ResultSet].
     * The statement is closed after this method returns.
     * @param connection a Jdbc Connection
     * @param sql a sql string with named parameters
     * @param parametersDto a data object to set the parameters,
     * or null if the statement doesn't have any parameters
     * @return the value or null of [ResultSet] doesn't have any rows
     */
    fun <T : Any> queryForScalar(
        connection: Connection,
        sql: String,
        parametersDto: Any?
    ): T? {
        return executeQuery(connection, sql, parametersDto) { rs ->
            rs.getScalar()
        }
    }

    fun execute(
        connection: Connection,
        sql: String,
        parametersDto: Any?
    ) {
        if (parametersDto == null) {
            return connection.createStatement().use { stmt ->
                stmt.execute(sql)
            }
        }
        else {
            return connection.prepareNpStatement(sql).use { stmt ->
                stmt.setParameters(parametersDto, mappingProvider)
                stmt.execute()
            }
        }
    }
}