package com.qualifiedcactus.sqlObjectMapper.toParam

interface ParamValueConverter {
    fun convert(value: Any?, objectCreator: JdbcObjectCreator): Any?
}