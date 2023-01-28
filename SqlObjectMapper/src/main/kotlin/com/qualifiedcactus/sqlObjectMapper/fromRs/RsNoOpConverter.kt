package com.qualifiedcactus.sqlObjectMapper.fromRs

/**
 * Does nothing
 */
class RsNoOpConverter : RsValueConverter {
    override fun convert(value: Any?): Any? = value
}