@file:JvmName("ConnectionNpUtils")
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
    fun setParameter(name: String, value: Any?) {
        setParameterUpperCased(name.uppercase(), value)
    }

    /**
     * Set parameter(s) using a data object (DTO)
     */
    fun setParametersByDto(dto: Any) {
        val paramMap = MappingProvider.mapParamClass(dto::class)
        val jdbcObjectCreator = JdbcObjectCreator(statement.connection)
        paramMap.valueExtractors.forEach { (paramName, parameterInfo) ->
            val parameterValue = parameterInfo.converter.convert(
                parameterInfo.getter(dto),
                jdbcObjectCreator
            )
            setParameterUpperCased(paramName, parameterValue)
        }
    }

    /**
     * Set parameter(s) using a map of case-insensitive names and values
     */
    fun setParametersByMap(valueMap: Map<String, Any?>) {
        valueMap.forEach { (paramName, paramValue) ->
            setParameter(paramName, paramValue)
        }
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

