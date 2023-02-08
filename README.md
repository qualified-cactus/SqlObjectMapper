# SqlObjectMapper

This is a library for using data objects (DTO) as parameters 
and parsing ResultSet's rows into DTO. 
Support bean-style and all-args-style creation for DTO.

##  Install package from Maven Central

Add maven dependency:

```xml
<dependency>
    <groupId>com.qualifiedcactus</groupId>
    <artifactId>sqlObjectMapper</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Quick start

In Kotlin:

```kotlin
class InDto(
    val column1: String
)

class OutDto(
    val column1: String,
    val column2: String
)

fun example(connection: Connection) {
    val results: List<OutDto> = connection
        .prepareNpStatement(
            "SELECT * FROM table_a WHERE column_1 = :column_1"
        )
        .setParametersByDto(InDto("bar"))
        .useExecuteQuery { it.toList(OutDto::class) }
}
```

In Java:

```java
public class InDto {
    private String column1;
    //... getters and setters omitted
}

public class OutDto {
    private String column1;
    private String column2;
    //... getters and setters omitted
}

public class Example {
    public void example(Connection connection) {
        List<OutDto> results = ConnectionNpUtils
            .prepareNpStatement(connection, "SELECT * FROM table_a WHERE column_1 = :column_1")
            .setParametersByDto(new InDto("bar"))
            .useExecuteQuery(rs-> ResultSetParser.parseToList(rs, OutDto.class));
    }
}
```

## IMPORTANT: Assumption about column names

This library assumes that the column names is case-insensitive and 
the column names taken from `java.sql.ResultSetMetaData.getColumnLabel()` 
is always in UPPERCASE (which is the behaviour HSQL and Oracle JDBC's drivers).

Since this assumption might be wrong for other JDBC drivers which you might use, 
please use uppercase column names or use avoid using case-sensitive columns names when that happens.

## Documentations

* [DTO to PreparedStatement's parameters](documentations/DTO_to_parameters.md)
* [ResultSet's rows to DTO](documentations/ResultSet_to_DTO.md)
* [Other helper classes and functions](documentations/Helpers_classes_and_functions.md)
* [API doc](https://qualified-cactus.github.io/SqlObjectMapper/)


## Performance benchmark

Performance of parsing 100 rows into simple DTOs (no nested or to-many). Measured by Java Benchmark Harness (JMH). 
See [the benchmark project](SqlObjectMapperBenchmark) for the implementation of the benchmark.

```
plainSqlBenchmark         avgt    5   5241.022 ±  83.903  ns/op
sqlObjectMapperBenchmark  avgt    5   5643.287 ±  62.899  ns/op
sql2oBenchmark            avgt    5   7642.332 ± 121.217  ns/op
springJpaBenchmark        avgt    5  32067.611 ± 588.319  ns/op
```


