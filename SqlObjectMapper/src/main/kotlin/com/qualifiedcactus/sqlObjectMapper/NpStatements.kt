/*
 * MIT License
 *
 * Copyright (c) 2023 qualified-cactus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

@file:JvmName("NpStatements")
package com.qualifiedcactus.sqlObjectMapper

import com.qualifiedcactus.sqlObjectMapper.toParam.JdbcObjectCreator
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Create [NpPreparedStatement] from a [Connection] and a SQL string with named parameter (for example ":foo", ":bar")
 */
fun Connection.prepareNpStatement(npSql: String): NpPreparedStatement {
    val query = NamedParameterQuery(npSql)
    return NpPreparedStatement(
        this.prepareStatement(query.translatedQuery),
        query
    )
}

/**
 * Create [NpCallableStatement] from a [Connection] and a SQL string with named parameter (for example ":foo", ":bar")
 */
fun Connection.prepareNpCall(npSql: String): NpCallableStatement {
    val query = NamedParameterQuery(npSql)
    return NpCallableStatement(
        this.prepareCall(query.translatedQuery),
        query
    )
}

abstract class NpStatement
internal constructor(
    val statement: PreparedStatement,
    val query: NamedParameterQuery
) {
    /**
     * Use this when the parameter name is already uppercase
     */
    internal fun setParameterUpperCased(upperCasedName: String, value: Any?) {
        val indexList = query.parameterIndexes[upperCasedName]
            ?: throw SqlObjectMapperException("Can't find parameter that match ${upperCasedName}")

        indexList.forEach { index -> statement.setObject(index, value) }
    }

    /**
     * Set a parameter by name
     * @param name a case-insensitive parameter name
     * @param value value of a parameter
     */
    fun setParameter(name: String, value: Any?): NpStatement {
        setParameterUpperCased(name.uppercase(), value)
        return this
    }

    /**
     * Set parameter(s) using a data object (DTO)
     */
    fun setParametersByDto(dto: Any): NpStatement {
        val paramMap = MappingProvider.mapParamClass(dto::class)
        val jdbcObjectCreator = JdbcObjectCreator(statement.connection)
        paramMap.valueExtractors.forEach { (paramName, parameterInfo) ->
            val parameterValue = parameterInfo.converter.convert(
                parameterInfo.getter(dto),
                jdbcObjectCreator
            )
            setParameterUpperCased(paramName, parameterValue)
        }
        return this
    }

    /**
     * Set parameter(s) using a map of case-insensitive names and values
     */
    fun setParametersByMap(valueMap: Map<String, Any?>): NpStatement {
        valueMap.forEach { (paramName, paramValue) ->
            setParameter(paramName, paramValue)
        }
        return this
    }

    /**
     * Execute the statement, execute [resultSetToValue] function
     * and finally close the statement
     */
    fun <T> useExecuteQuery(resultSetToValue: (rs: ResultSet)-> T): T {
        return statement.use {
            statement.executeQuery().use(resultSetToValue)
        }
    }

    /**
     * Execute the statement and close it.
     */
    fun useExecuteUpdate(): Int {
        return statement.use {
            statement.executeUpdate()
        }
    }

    /**
     * Execute the statement and close it.
     */
    fun useExecuteLargeUpdate(): Long {
        return statement.use {
            statement.executeLargeUpdate()
        }
    }
}

/**
 * A named parameter(s) version of [PreparedStatement]
 */
class NpPreparedStatement
internal constructor(
    statement: PreparedStatement,
    query: NamedParameterQuery
) : PreparedStatement by statement, NpStatement(statement, query)

/**
 * A named parameter(s) version of [CallableStatement]
 */
class NpCallableStatement
internal constructor(
    statement: CallableStatement,
    query: NamedParameterQuery
) : CallableStatement by statement, NpStatement(statement, query)

