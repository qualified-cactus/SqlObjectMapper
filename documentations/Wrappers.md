# Convenient wrappers

## QueryExecutor

Use `QueryExecutor` to conveniently execute your query and get query's results.

Without `QueryExecutor`:

```kotlin
val queriedEntity: Entity1? = connection
    .prepareNpStatement("SELECT * FROM entity_1 WHERE column_2 = :param_1")
    .use { stmt ->
        stmt.setParameters(QueryInput(3), cmProvider)
        stmt.execute()
        MappedResultSet(stmt.resultSet, cmProvider).toObject(Entity1::class.java)
    }
```

With `QueryExecutor`:

```kotlin
val queryExecutor = QueryExecutor(cmProvider)
val queriedEntity: Entity1? = queryExecutor.queryForObject(connection,
    "SELECT * FROM entity_1 WHERE column_2 = :param_1",
    QueryInput(3),
    Entity1::class.java
)
```

## Transaction wrapper

You can use `TransactionManager.executeTransaction` to conveniently manage your transaction

```kotlin
TransactionManager.executeTransaction(connection, Connection.TRANSACTION_READ_UNCOMMITTED) { conn ->
    val queriedEntity: Entity1? = QueryExecutor(cmProvider).queryForObject(conn,
        "SELECT * FROM entity_1 WHERE column_2 = :param_1",
        QueryInput(3),
        Entity1::class.java
    )
}
```