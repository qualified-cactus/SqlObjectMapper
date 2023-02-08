# ResultSet to DTO

Parse `ResultSet`'s row(s) to DTO using the following extension functions.

```kotlin
fun <T : Any> ResultSet.toScalar(): T? // get first column of first row
fun <T : Any> ResultSet.toObject(clazz: KClass<T>): T? // get first row as a DTO
fun <T : Any> ResultSet.toList(clazz: KClass<T>): List<T> // get rows as a list of DTO
```

For Java users, use the static methods
from `com.qualifiedcactus.sqlObjectMapper.fromRs.ResultSetParser`.



## Mapping a data object class

### ResultSet -> Simple DTO

```kotlin
// all-args style
class MyDataObject(
    val myColumn1: String,
    val myColumn2: String
)
// or bean style
class MyDataObject {
    lateinit var myColumn1: String
    lateinit var myColumn2: String
    @RsIgnore // use RsIgnore to ignore a property when using bean style
    lateinit var ignoredProperty: String
}
```

A simple class like `MyDataObject` will have values from columns `MY_COLUMN_1` and `MY_COLUMN_2`.
Use the `RsConstructor` annotation to specify which constructor to use 
if there are more than 1 public constructors. 
Bean style is used if the selected constructor has no params.

Use `RsColumn` to specify which column to use,
how the value is converted and if the column is an ID column (for to-one DTO and to-many collection)

```kotlin
class MyDataObject(
    // will take value from "MY_SPECIFIC_COLUMN" column instead.
    @RsColumn(name = "my_specific_column", converter = MyConverter::class, isId = true)
    val myColumn1: String,
    val myColumn2: String
)
```

Converter classes are classes with a public no-args constructor and implement 
the `com.qualifiedcactus.sqlObjectMapper.fromRs.RsValueConverter` interface 

### ResultSet -> DTO with nested/to-one object(s)

Sometimes we want to nest a DTO instead another DTO 
(to make the JSON string of this object more readable, for instance).
We do that by using the `RsToOne` / `RsNested` annotations.

```kotlin
class MyDto(
    val column1: String,
    @RsNested
    val innerDto: NestedDto
)

class NestedDto(
    val column2: String
)
```

`RsToOne` is the same as `RsNested`, except that it requires 
the class of its property to have at least 1 column marked as an ID column.
If the ID column is null, then the property is null.

**NOTE:** having `RsToOne`, `RsNested` or `RsToMany` in your DTO will make the algorithm switch 
from using `ResultSet.getObject(columnIndex)` to using `ResultSet.getObject(columnName)`. 
`getObject(columnName)` is a bit slower than `getObject(columnIndex)`.


### ResultSet -> DTO with to-many collection(s)

Sometimes we want to capture one-to-many relationship
as a list of parent DTOs that each have a list of child DTOs.

This can be done by:

* Doing n + 1 queries (query a list of parents, then for each parent, query a list of their children)
* Doing a single JOIN query

You can use the `RsToMany` annotation to capture one-to-many relationship into DTOs from the results of a single JOIN query.

```kotlin
class MyDto(
    // Must have at least 1 column marked as ID. 
    // Mark more than 1 columns to emulate the behaviour of composite keys.
    @RsColumn(isId = true) 
    val column1: String,
    @RsToMany
    val innerDto: List<ChildDto>
)

class ChildDto(
    @RsColumn(isId = true) // Must have at least 1 column marked as ID
    val column2: String,
    val column3: String
)
```

**NOTE:** Since the algorithm doesn't know if the ID is ordered, 
a hashmap of hashmap is used to track the IDs, 
so a `ResultSet` with many rows may cause memory problems.