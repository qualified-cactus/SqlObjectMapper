# DTO to parameters

This library provide [classes and functions](../SqlObjectMapper/src/main/kotlin/com/qualifiedcactus/sqlObjectMapper/NpStatements.kt) 
that support case-insensitive, named parameters in your sql string.

## Creating a named parameter(s) statement

Use the following function to create a NpStatement.
`NpPreparedStatement` and `NpCallableStatement` 
implements `PreparedStatement` and `CallableStatement`, respectively.

```kotlin
// equivalent of Connection.prepareStatement()
fun Connection.prepareNpStatement(npSql: String): NpPreparedStatement

// equivalent of Connection.prepareCall()
fun Connection.prepareNpCall(npSql: String): NpCallableStatement
```

## Setting parameter(s)

### By name

Use `NpStatement.setParameter(name, value)`.

### By DTO

Use `NpStatement.setParametersByDto(dto)`.

#### Map the DTO

Map your DTO similar to how you map your DTO with `RsColumn`, `RsNested` and `RsIgnore`.

```kotlin
class MyDto(
    val param1: String,
    // A converter class implements ParamValueConverter and has a public no-arg constructor.
    @Param(name = "other_param", converter = MyConverter::class) 
    val param2: String,
    @NestedParams
    val nestedParam: NestedParam,
    @IgnoreParam
    val ignoredProperty: String
)

class NestedParam(
    val param3: String,
    val param4: String
)
```

### By map

Use `NpStatement.setParametersByMap(mapOfNameAndValue)`.