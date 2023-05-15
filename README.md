# SqlObjectMapper

This library add named parameter, data object handling to Java's JDBC and other quality of life features.
Support bean-style and all-args-style creation for DTO. 
Written to be easy to use for both Java and Kotlin users.

##  Install package from Maven Central

Add the latest maven dependency:

```xml
<dependency>
    <groupId>com.qualifiedcactus</groupId>
    <artifactId>sqlObjectMapper</artifactId>
    <version>4.1.3</version>
</dependency>
```

## Quick example

```kotlin

val sql = "select count(*) from table_1 where column_1 = :param_1"
val count = connection.prepareStmt(sql).use {stmt->
    stmt.setParametersByDto(ParamDto(1))
        .executeThen()
        .processScalarResult(Long::class).firstOnly()!!
}

```

## IMPORTANT: Assumption about column names

This library assumes that the column names is case-insensitive and
the column names taken from `java.sql.ResultSetMetaData.getColumnLabel()`
is always in UPPERCASE (which is the behaviour HSQL and Oracle JDBC's drivers).

Since this assumption might be wrong for other JDBC drivers which you might use,
please use uppercase column names or use avoid using case-sensitive columns names when that happens.


## Documentations

### Create a named parameter statement

```kotlin

val connection: Connection = dataSource.connection
val sql = """
    select column_1, column_2 
    from table_1 
    where column_1 = :param_1 and column_2 in :param_2[3]
"""
val stmt = connection.prepareStmt(sql)

```

Note that the `prepareStmt` extension function belongs to the companion object of `NpStatement`.
The sql string above will be translated to:

```
select column_1, column_2 
from table_1 
where column_1 = ? and column_2 in (?,?,?)
```

`:param_1` (simple parameter) is replaced with `?` and `:param_2[3]` (expanded parameter) is replaced with `(?,?,?)`.
A named parameter can appear at multiple places. 
Using unnamed parameter (`?`) in a named-parameter SQL string is also possible, but it is not recommended to do so.

### Setting parameter(s) by name

Use `setParameter` or `setParametersByDto`.
An expanded parameter (which `param_2` is) requires value to be castable to Collection<Any?> 
and the collection's size must be less or equal to the integer specified inside the square brackets.
 
You can set parameters directly:

```kotlin
stmt.setParameter("param_1", 1)
stmt.setParameter("param_2", listOf(1,2,3))
```

Or you can set parameters using a data object.
Names of DTO's properties are automatically converted to snake-cased name.
You can use annotations `SqlParam`, `NestedParams` or `IgnoreParam` from the package 
`com.qualifiedcactus.sqlObjectMapper.toParam` to customize your DTO.

```kotlin
data class Dto(
    val param1: Int,
    @NestedParams
    val nested: NestedDto,
)
data class NestedDto(
    val param2: List<Int>,
)
stmt.setParametersByDto(Dto(
    1,
    NestedDto(
        listOf(1,2,3)
    )
))
```


### Getting values as DTO/scalar from ResultSet

Use `NpStatement.processDtoResult` for DTO and `NpStatement.processScalarResult`
for scalar (first column's value). Both methods return a `ResultSetProcessor<T>`, 
which have these methods for getting the data:

- `toList` and `toSet` return a list and a set of data, respectively.
- `firstOnly` returns first row's data or null if there is no row.
- `toStream` returns a **CLOSABLE** stream of data, which is useful if your database driver supports setting fetch size.

```kotlin
data class ResultSetDto(
    val column1: Int,
    val column2: Int,
)
val dtoList: List<ResultSetDto> = stmt.executeThen()
    .processDtoResult(ResultSetDto::class).toList()
```

```kotlin
val scalarList: List<Int> = stmt.executeThen()
    .processScalarResult(Int::class).toList()
```

You can feature specify your DTO using annotations 
`RsColumn`, `RsNested`, `RsToMany` (parent table left join child table query), 
`RsRecursive` (recursive common table expression query) and `RsIgnore` from
the package `com.qualifiedcactus.sqlObjectMapper.fromRs` (see Javadocs for more details).

### Getting auto generated keys from an insert statement

Create a statement with the extension function `prepareStmtWithGeneratedKeys` in `NpStatement` 
to tell JDBC to retrieve the generated key(s). 
Both single-columns and multi-columns key are supported by the classes `SingleAutoGenKey` and `CompositeAutoGenKey` 
in the package `com.qualifiedcactus.sqlObjectMapper.fromRs.autoGenKeys`.

Creating single-column generated key declaration:

```kotlin
val keyDeclaration = SingleAutoGenKey("column_1", Int::class)
```

Creating multi-columns generated key declaration:

```kotlin
data class GenerateKeyDto(
    val column1: Int,
    val column2: Int,
)
val keyDeclaration = CompositeAutoGenKey(GenerateKeyDto::class)
```

Creating a statement with generated key:

```kotlin
val stmt = connection.prepareStmtWithGeneratedKeys(sqlString, keyDeclaration)
```

Retrieve the key after statement's execution:

```kotlin
val resultSetProcessor = stmt.processAutoGenKeys()
```





## Others

* [API doc](https://qualified-cactus.github.io/SqlObjectMapper/)
* [Change logs](CHANGELOGS.md)