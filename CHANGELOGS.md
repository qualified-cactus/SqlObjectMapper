# Change logs

## 3.1.0

* Added several overloaded methods for `Connection.prepareNpCall` 
and `Connection.prepareNpStatement` to match its `Connection.prepareStatement` and `Connection.prepareCall` equivalent.

* Added and `DeclaredGeneratedKeys` and `NpStatement.executeInsert` 
to support getting generated key(s) from insert statement (See api docs from more details).

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