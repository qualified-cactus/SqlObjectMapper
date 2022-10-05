import java.sql.CallableStatement
import java.sql.Connection
import java.sql.PreparedStatement



abstract class NpStatement(
    protected val statement: PreparedStatement,
    protected val npQuery: NamedParameterQuery
) {
    fun setParam(paramName: String, value: Any?) {
        val indexes = npQuery.parameterIndexes[paramName]
            ?: throw SqlObjectMapperException("Parameter ${paramName} doesn't exist")
        for (index in indexes) {
            statement.setObject(index, value)
        }
    }

    fun setParamsFromDto(dto: Any) {
        val valueMap = SqlObjectMapper.getClassMapping(dto.javaClass)
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
        return MappedResultSet(statement.resultSet)
    }
}

class NpPreparedStatement
private constructor(
    statement: PreparedStatement,
    npQuery: NamedParameterQuery
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
    npQuery: NamedParameterQuery
) : NpStatement(statement, npQuery), CallableStatement by statement {
    companion object {
        @JvmStatic
        fun Connection.prepareNpCall(npSql: String): NpCallableStatement {
            val npQuery = NamedParameterQuery(npSql)
            return NpCallableStatement(this.prepareCall(npQuery.translatedQuery), npQuery)
        }
    }
}


