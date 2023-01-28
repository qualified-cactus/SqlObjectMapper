package com.qualifiedcactus.sqlObjectMapper.toParam

class ParamNoOpConverter : ParamValueConverter {
    override fun convert(value: Any?, objectCreator: JdbcObjectCreator): Any? = value

}