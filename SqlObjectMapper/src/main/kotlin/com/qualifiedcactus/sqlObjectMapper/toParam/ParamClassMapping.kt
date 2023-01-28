package com.qualifiedcactus.sqlObjectMapper.toParam

internal interface ParamClassMapping {
    val valueExtractors: Map<String, Parameter>

    class Parameter(
        val getter: (o: Any) -> Any?,
        val converter: ParamValueConverter
    )
}