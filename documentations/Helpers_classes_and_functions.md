# Helper classes and functions

## SQL string builder

Use `NpSqlStringBuilder` to make writing dynamic query for raw SQL easier.

```kotlin
class QueryDto(
    val param1: String?,
    val param2: String?,
)

fun example(conn: Connection, query: QueryDto) {
    val builder = NpSqlStringBuilder(conn, "SELECT * FROM table_a WHERE TRUE")

    if (query.param1 != null) {
        builder.append(" AND column_1 = :param_1")
            .addParameter("param_1", query.param1)
    }
    if (query.param2 != null) {
        builder.append(" AND column_2 = :param_2")
            .addParameter("param_2", query.param2)
    }

    val result = builder.toNpPreparedStatement()
        .useExecuteQuery { it.toList(OutDto::class) }
}
```

## Transaction wrapper

Use extension function `Connection.executeTransaction` (or the static method `TransactionHelper.executeTransaction` in Java) 
to wrap a function inside a transaction.

```kotlin
fun example(conn: Connection) {
    connection.executeTransaction {
        val result = prepareNpStatement("SELECT * FROM table_a")
            .useExecuteQuery { it.toList(OutDto::class) }
    }
}
```