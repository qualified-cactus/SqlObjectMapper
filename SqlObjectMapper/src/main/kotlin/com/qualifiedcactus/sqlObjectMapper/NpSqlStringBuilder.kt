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

package com.qualifiedcactus.sqlObjectMapper

import com.qualifiedcactus.sqlObjectMapper.toParam.JdbcObjectCreator
import java.sql.Connection

/**
 * A convenient class that helps with building dynamic raw sql query
 */
class NpSqlStringBuilder
@JvmOverloads
constructor(
    val connection: Connection,
    private val startingString: String = ""
) {


    private val jdbcObjectCreator = JdbcObjectCreator(connection)
    private val stringBuilder = StringBuilder(startingString)

    /**
     * A Map of uppercase parameter name and parameter value
     */
    private val parameterValues = HashMap<String, Any?>()

    /**
     * Append a string to sql query
     */
    fun append(s: String): NpSqlStringBuilder {
        stringBuilder.append(s)
        return this
    }

    fun toQueryString(): String {
        return stringBuilder.toString()
    }

    fun addParameter(paramName: String, paramValue: Any?): NpSqlStringBuilder {
        parameterValues[paramName.uppercase()] = paramValue
        return this
    }

    fun addParametersByDto(dto: Any): NpSqlStringBuilder {
        val mapping = MappingProvider.mapParamClass(dto::class)
        mapping.valueExtractors.forEach { (paramName, paramInfo)->
            parameterValues[paramName] = paramInfo.converter.convert(paramInfo.getter(dto), jdbcObjectCreator)
        }
        return this
    }

    /**
     * Create a [NpPreparedStatement] with the previously added parameters
     */
    fun toNpPreparedStatement(): NpPreparedStatement {
        val npStatement = connection.prepareNpStatement(stringBuilder.toString())
        parameterValues.forEach { (paramName, paramValue) ->
            npStatement.setParameterUpperCased(paramName, paramValue)
        }
        return npStatement
    }

    /**
     * Create a [NpCallableStatement] with the previously added parameters
     */
    fun toNpCallableStatement(): NpCallableStatement {
        val npStatement = connection.prepareNpCall(stringBuilder.toString())
        parameterValues.forEach { (paramName, paramValue) ->
            npStatement.setParameterUpperCased(paramName, paramValue)
        }
        return npStatement
    }
}