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

import annotationProcessing.JdbcObjectCreator
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.PreparedStatement



abstract class NpStatement(
    protected val statement: PreparedStatement,
    protected val npQuery: NamedParameterQuery,
) {
    fun setParameter(paramName: String, value: Any?) {
        val indexes = npQuery.parameterIndexes[paramName]
            ?: throw SqlObjectMapperException("Parameter ${paramName} doesn't exist")
        for (index in indexes) {
            statement.setObject(index, value)
        }
    }

    fun setParameters(dto: Any, classMappingProvider: ClassMappingProvider) {
        val valueMap = classMappingProvider.getClassMapping(dto.javaClass)
            .getColumnNameValueMap(JdbcObjectCreator(statement.connection), dto)
        for ((paramName, value) in valueMap) {
            setParameter(paramName, value)
        }
    }

    fun setParamsFromMap(values: Map<String, Any?>) {
        for ((colName, colValue) in values) {
            setParameter(colName, colValue)
        }
    }

    fun getMappedResultSet(classMappingProvider: ClassMappingProvider): MappedResultSet {
        return MappedResultSet(statement.resultSet, classMappingProvider)
    }
}

class NpPreparedStatement
private constructor(
    statement: PreparedStatement,
    npQuery: NamedParameterQuery,
) : NpStatement(statement, npQuery), PreparedStatement by statement {
    companion object {
        @JvmStatic
        fun Connection.prepareNpStatement(npSql: String): NpPreparedStatement {
            val npQuery = NamedParameterQuery(npSql)
            return NpPreparedStatement(this.prepareStatement(npQuery.translatedQuery), npQuery)
        }
    }
}


class NpCallableStatement
private constructor(
    statement: CallableStatement,
    npQuery: NamedParameterQuery,
) : NpStatement(statement, npQuery), CallableStatement by statement {
    companion object {
        @JvmStatic
        fun Connection.prepareNpCall(npSql: String): NpCallableStatement {
            val npQuery = NamedParameterQuery(npSql)
            return NpCallableStatement(this.prepareCall(npQuery.translatedQuery), npQuery)
        }
    }
}


