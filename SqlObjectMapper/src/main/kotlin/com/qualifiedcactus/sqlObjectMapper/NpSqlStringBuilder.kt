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

import java.sql.Connection

/**
 * A convenient class that helps with building dynamic raw sql query
 */
class NpSqlStringBuilder
@JvmOverloads
constructor(
    val connection: Connection,
    startingString: String = ""
) {


    private val stringBuilder = StringBuilder(startingString)
    private val paramList = ArrayList<ParameterWrapper>()

    /**
     * Append a string to sql query
     */
    fun append(s: String): NpSqlStringBuilder {
        stringBuilder.append(s)
        return this
    }

    /**
     * Return built sql string. Useful for debugging purpose
     */
    fun toQueryString(): String {
        return stringBuilder.toString()
    }

    /**
     * Add a parameter value of [paramName]
     */
    fun withValue(paramName: String, paramValue: Any?): NpSqlStringBuilder {
        paramList.add(ParameterWrapper.NonDtoWrapper(paramName, paramValue))
        return this
    }

    /**
     * Add a DTO containing parameters to this builder
     */
    fun withDto(dto: Any): NpSqlStringBuilder {
        paramList.add(ParameterWrapper.DtoWrapper(dto))
        return this
    }

    /**
     * Append parameters for SQL IN clause.
     * For example, if a parameter name is my_param and [paramValues]'s size is 3,
     * a string " (:my_param__0,:my_param__1,:my_param__2) " will be appended
     * and parameters values will be added to this builder.
     *
     * Note that IN clause have variable limit. The exact limit depends on the database you use
     * (1000 for Oracle, for example).
     */
    fun <T:Any> withInVariables(paramName: String, paramValues: List<T>) {
        stringBuilder.append(" (")
        paramValues.forEachIndexed { index, paramValue ->
            val indexedName = paramName + "__" + index
            paramList.add(ParameterWrapper.NonDtoWrapper(indexedName, paramValue))
            stringBuilder.append(indexedName)
            if (index < paramValues.size - 1) {
                stringBuilder.append(',')
            }
        }
        stringBuilder.append(") ")
    }

    /**
     * Create a [NpPreparedStatement] with the previously added parameters
     */
    fun toNpPreparedStatement(): NpPreparedStatement {
        val npStatement = connection.prepareNpStatement(stringBuilder.toString())
        paramList.forEach { param ->
            when (param) {
                is ParameterWrapper.DtoWrapper -> npStatement.setParametersByDto(param.value)
                is ParameterWrapper.NonDtoWrapper -> npStatement.setParameter(param.paramName, param.value)
            }
        }

        return npStatement
    }

    /**
     * Create a [NpCallableStatement] with the previously added parameters
     */
    fun toNpCallableStatement(): NpCallableStatement {
        val npStatement = connection.prepareNpCall(stringBuilder.toString())
        paramList.forEach { param ->
            when (param) {
                is ParameterWrapper.DtoWrapper -> npStatement.setParametersByDto(param.value)
                is ParameterWrapper.NonDtoWrapper -> npStatement.setParameter(param.paramName, param.value)
            }
        }
        return npStatement
    }


    private interface ParameterWrapper {
        class DtoWrapper(val value:Any) : ParameterWrapper
        class NonDtoWrapper(val paramName: String, val value:Any?) : ParameterWrapper
    }
}