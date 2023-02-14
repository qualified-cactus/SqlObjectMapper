# Getting auto generated key(s) from insert statement


## Declaring key

Declare auto generate key as follows:

### Single column key

```kotlin
val autoGenKey = SingleAutoGenKey(
    "column_1", // name of the column
    Int::class // data type of the column
)
```

### Multi-columns key

#### Declare DTO

Declare DTO with `RsColumn` and `RsNested` annotations.

The following examples declare a composite key made of columns `column_1` and `column_2`:

Simple DTO:

```kotlin
class CompositeKeyDto(
    val column1: Int,
    val column2: Int
)
```

Explicitly defined column(s):

```kotlin
class CompositeKeyDto(
    @RsColumn("column_1")
    val firstColumn: Int,
    @RsColumn("column_2")
    val secondColumn: Int
)
```

Arbitrary nested DTO(s) :

```kotlin
class CompositeKeyDto(
    val column1: Int,
    @RsNested
    val nested: NestedDto
)
class NestedDto(
    val column2: Int
)
```


## Get generated key from executeUpdate()

Use one of the methods `executeUpdateWithGeneratedKeys`, 
`useExecuteUpdateWithGeneratedKeys` or `getGeneratedKeysList` in `NpStatement`.

Example:

```kotlin
val declaredKey = CompositeAutoGenKey(CompositeKeyDto::class)
val generatedKeys: List<CompositeKeyDto> = connection
    .prepareNpStatement(
        "INSERT INTO table_2 VALUES (NEXT VALUE FOR seq_2, NEXT VALUE FOR seq_3)",
        declaredKey
    )
    .useExecuteUpdateWithGeneratedKeys(declaredKey)
```