package com.qualifiedcactus.sqlObjectMapper.fromRs

interface RsValueConverter {
    fun convert(value: Any?): Any?
}