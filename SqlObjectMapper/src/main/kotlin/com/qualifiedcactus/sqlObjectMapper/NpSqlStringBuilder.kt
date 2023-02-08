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