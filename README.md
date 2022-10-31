# SqlObjectMapper

This is a library that extends the existing JDBC API 
so that data objects can be used as input (to set parameters) and output (from ResultSet's rows).

Two kinds of data object is currently supported:
* Java's Bean
* Kotlin's data class (all parameters of primary constructors must also be properties)

## Documentations

* [Extension to PreparedStatement and CallableStatement](documentations/NpStatement.md)
* [Extension to ResultSet](documentations/MappedResultSet.md)
* [Convenient wrappers](documentations/Wrappers.md)
* [API doc](https://qualified-cactus.github.io/SqlObjectMapper/)


## Quick start

#### 1. Download the package

Download the package from maven central:

```xml
<dependency>
  <groupId>com.qualifiedcactus</groupId>
  <artifactId>sqlObjectMapper</artifactId>
  <version>1.1.0</version>
</dependency>
```

#### 2. Set default class mapping provider

If you use Java, pick `sqlObjectMapper.annotationProcessing.bean.BeanMappingProvider`.
```
ClassMappingProvider.setDefaultClassMappingProvider(new BeanMappingProvider())
```

If you use Kotlin, pick `sqlObjectMapper.annotationProcessing.dataClass.DataClassMappingProvider`.
```kotlin
ClassMappingProvider.defaultClassMappingProvider = DataClassMappingProvider()
```

_Note: There should be only 1 instance of each type of `ClassMappingProvider`
(2 mentioned above) because the cache is stored in each instance._

#### 3. Define your data object

Kotlin Data Class:

```kotlin
data class QueryInput(
    val param1: String,
    val param2: String
)

data class Entity1(
    val column1: Int,
    val column2: String,
    val column3: String,
)
```

Or Java Bean (with lombok):

```java
@Data
public class QueryInput {
    private String param1;
    private String param2;
}

@Data
public class Entity1 {
    private Integer column1;
    private String column2;
    private String column3;
}
```

#### 4. Execute Query with NpPreparedStatement or NpCallableStatement

Use convenient class `QueryExecutor` to set query parameters using a DTO 
and put the result of a query into a list of DTOs.

In kotlin:

```kotlin

val entityList: List<Entity1> = QueryExecutor().queryForList(
    connection, 
    """
    SELECT column_1, column_2, column_3 
    FROM entity_1 
    WHERE column_2 LIKE :param_1 AND column_3 LIKE :param_2
    """,
    QueryInput("abc%", "%def"),
    Entity1::class.java
)
```

In Java:

```java
public class Main {
    public static void main(String[] args) {
        List<Entity1> entityList = new QueryExecutor().queryForList(
            connection,
            """
            SELECT column_1, column_2, column_3
            FROM entity_1
            WHERE column_2 LIKE :param_1 AND column_3 LIKE :param_2
            """,
            new QueryInput("abc%", "%def"),
            Entity1.class
        );
    }
}
```

For more examples, see these unit tests:

* [Java example](src/test/java/example/JavaExampleTest.java)
* [Kotlin example](src/test/kotlin/example/KotlinExampleTest.kt)


## Motivation

In web development, your interactions with data object tends to be in the following order:

1. Receive data object from your json parser
2. Validate it
3. Parse the data object into your sql parameters and execute a query
4. Parse the result of a query in to a data object
5. Send that data object as a response body

In step 3 and 4, you have the options of either do it by hand or automate it using Hibernate,
or many other ORM libraries. However, Hibernate sometimes feel too restrictive
because too much is abstracted away. That is why I created this library that
simply add a data-object-parsing feature to Java's JDBC API.

