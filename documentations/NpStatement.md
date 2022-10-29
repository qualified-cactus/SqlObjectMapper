
# Extension to PreparedStatement and CallableStatement


`PreparedStatement` and `CallableStatement` are extended by `NpPreparedStatement` and `NpCallableStatement`, using
[Kotlin's Delegation](https://kotlinlang.org/docs/delegation.html).


## Creating NpPreparedStatement and NpCallableStatement

`NpPreparedStatement` and `NpCallableStatement`'s companion objects have 
[extension function](https://kotlinlang.org/docs/extensions.html#extension-functions), namely `prepareNpStatement`
and `prepareNpCall`, which have `java.sql.Connection` as the receiver type to build themselves. 
Those function has identical parameter `java.sql.Connection`'s `prepareStatement` and `prepareCall`.

Example:

```kotlin
connection.prepareNpStatements("SELECT * FROM table_1 WHERE col_1 = :param_1")
connection.prepareNpCall("{call procedure_1(:param_1, :param_2)}")
```


## Setting parameters by names and data objects

Both `NpCallableStatement` and `NpPreparedStatement` has this feature.

### By name and value

```kotlin
npPreparedStatement.setParameter(name, value)
```

### By a map of names and values

```kotlin
npPreparedStatement.setParamsFromMap(map)
```

### By a data object

```kotlin
// a singleton instance of data class mapping provider
val cmProvider = DataClassMappingProvider()


data class Input(
    val param1: String,
    val param2: String
)

npPreparedStatement.setParameters(Input("abc", "def"), cmProvider)
```

#### Specify column name

Use `sqlObjectMapper.annotations.MappedProperty` annotation to specify column name 

```kotlin
data class Input(
    val param1: String,
    @MappedProperty(name = "custom_name")
    val param2: String
)
```

#### Value converter

If you want to converter the value of a property into another value before setting a parameter, 
define a class that inherit `sqlObjectMapper.annotations.ValueConverter` class and register it to a 
property using `sqlObjectMapper.annotations.MappedProperty` annotation.

```kotlin
// define your value converter

class MyConverter : ValueConverter {
    override fun toDb(jdbcObjectCreator: JdbcObjectCreator, value: Any?): Any? {
        val s = value as String
        return s.length
    }
}

data class Input(
    val param1: String,
    @MappedProperty(name = "custom_name", valueConverter = MyConverter::class)
    val param2: String
)
```

Note: you can use `JdbcObjectCreator` to convert your value to `java.sql.Array`, `java.sql.Blob` and other JDBC specific types.

#### Nested

If you want to use nested objects, use `sqlObjectMapper.annotations.Nested` annotation.

```kotlin
data class Input(
    val param1: String,
    @Nested
    val nestedInput: NestedInput
)

data class NestedInput(
    @MappedProperty(name = "custom_name", valueConverter = MyConverter::class)
    val param2: String
)
```

#### Ignore a property

To ignore a property, put `sqlObjectMapper.annotations.IgnoredProperty` annotation on that property


```kotlin
data class Input(
    val param1: String,
    @IgnoredProperty
    val ignoreMe: String
)
```