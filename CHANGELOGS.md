# Change logs

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