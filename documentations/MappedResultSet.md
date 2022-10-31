# Extension to ResultSet

`java.sql.ResultSet` is extended by `sqlObjectMapper.MappedResultSet`, using
[Kotlin's Delegation](https://kotlinlang.org/docs/delegation.html).

## Creating an MappedResultSet object

```kotlin
val mappedRs = MappedResultSet(statement.resultSet)
```

## Get next row data as data object

```kotlin
data class Entity1(
    val column1: String,
    val column2: String,
    @MappedProperty(name = "custom_column_name", valueConverter = MyValueConverter::class)
    val column3: Int
)

val entity: Entity1 = mappedRs.toObject(Entity1::class.java)
```

### Left join to one other entity

```kotlin
// Entity1 --o| Entity2
data class Entity1(
    val column1: String,
    val column2: String,
    @JoinOne
    val entity2: Entity2? // will be null if entity2's id column is null
)

data class Entity2(
    @MappedProperty(isId = true)
    val foo: Int,
    var bar: String
)

val entity: Entity1 = mappedRs.toObject(Entity1::class.java)
```

There can be multiple `JoinOne` annotated properties in a same class

## Get all rows data as a collection of data objects

```kotlin
val entity: List<Entity1> = mappedRs.toList(Entity1::class.java)
```

### Get all rows data from ResultSet of a left join to many query

```kotlin
// ParentEntity --o{ ChildEntity

data class ParentEntity(
    @MappedProperty(isId=true)
    val column1: String,
    val column2: String,
    @JoinMany
    val children: List<ChildEntity>
)

data class ChildEntity(
    @MappedProperty(isId=true)
    val foo: String,
    val bar: String
)

val entity: List<ParentEntity> = mappedRs.toList(ParentEntity::class.java)
```

There can be multiple `JoinMany` annotated properties in a same class

_Note: `MappedResultSet` use hashmap cho manage ids of objects when `JoinMany` annotation is used.
Take that into consideration when doing optimization._