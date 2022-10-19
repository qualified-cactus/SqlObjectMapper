package sqlObjectMapper

import annotationProcessing.JdbcObjectCreator
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.PreparedStatement



abstract class NpStatement(
    protected val statement: PreparedStatement,
    protected val npQuery: NamedParameterQuery,
    protected val classMappingProvider: ClassMappingProvider
) {
    fun setParam(paramName: String, value: Any?) {
        val indexes = npQuery.parameterIndexes[paramName]
            ?: throw SqlObjectMapperException("Parameter ${paramName} doesn't exist")
        for (index in indexes) {
            statement.setObject(index, value)
        }
    }

    fun setParamsFromDto(dto: Any) {
        val valueMap = classMappingProvider.getClassMapping(dto.javaClass)
            .getColumnNameValueMap(JdbcObjectCreator(statement.connection), dto)
        for ((paramName, value) in valueMap) {
            setParam(paramName, value)
        }
    }

    fun setParamsFromMap(values: Map<String, Any?>) {
        for ((colName, colValue) in values) {
            setParam(colName, colValue)
        }
    }

    fun getMappedResultSet(): MappedResultSet {
        return MappedResultSet(statement.resultSet, classMappingProvider)
    }
}

class NpPreparedStatement
private constructor(
    statement: PreparedStatement,
    npQuery: NamedParameterQuery,
    classMappingProvider: ClassMappingProvider
) : NpStatement(statement, npQuery, classMappingProvider), PreparedStatement by statement {
    companion object {
        @JvmStatic
        fun ClassMappingProvider.prepareNpStatement(conn: Connection, npSql: String): NpPreparedStatement {
            val npQuery = NamedParameterQuery(npSql)
            return NpPreparedStatement(conn.prepareStatement(npQuery.translatedQuery), npQuery, this)
        }
    }
}


class NpCallableStatement
private constructor(
    statement: CallableStatement,
    npQuery: NamedParameterQuery,
    classMappingProvider: ClassMappingProvider
) : NpStatement(statement, npQuery, classMappingProvider), CallableStatement by statement {
    companion object {
        @JvmStatic
        fun ClassMappingProvider.prepareNpCall(conn: Connection, npSql: String): NpCallableStatement {
            val npQuery = NamedParameterQuery(npSql)
            return NpCallableStatement(conn.prepareCall(npQuery.translatedQuery), npQuery, this)
        }
    }
}


