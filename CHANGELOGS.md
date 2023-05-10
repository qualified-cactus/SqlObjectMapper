# Change logs

## 4.1.1

### Breaking changes:

- API overhaul: most of the api is changed, see documentations for details.
- Change license from MIT to AGPL


## 3.2.0

* Added `ParamUuidToByteArrayConverter` and `RsByteArrayToUuidConverter`
* Added `RsPage<T>`, which is almost the same as Spring Data's 
[`Page<T>`](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/Page.html), 
in case when Spring Data dependency is not available.

## 3.1.0

* Added several overloaded methods for `Connection.prepareNpCall` 
and `Connection.prepareNpStatement` to match its `Connection.prepareStatement` and `Connection.prepareCall` equivalent.

* Added and `DeclaredGeneratedKeys`, `NpStatement.executeUpdateWithGeneratedKeys` and `NpStatement.getGeneratedKeysList` 
to support getting generated key(s) from insert statement.

* Added `ResultSet.toScalarList` (or `ResultSetParser.parseToScalarList` for java users) 
to support getting a list of values from a single columns.

## 3.0.0

### Breaking Change

Changed the signature of `RsValueConverter.convert` from `convert(value)` to `convert(value, propertyType)` 
so that you can reuse a converter to convert to different types.

### Additional feature

Added `RsStringToEnumConverter` and `ParamEnumToStringConverter` to be used with enum values.

## 2.0.1

Allow to-many collection accept null element (when converted)

## 2.0.0

Rewrite code to improve performance.