package kotlinImpl

import annotationProcessing.ClassMapping
import annotationProcessing.GlobalClassInfo
import sqlObjectMapper.ClassMappingProvider
import sqlObjectMapper.NameConverter
import sqlObjectMapper.SqlObjectMapperUtils

class DataClassMappingProvider(
    val nameConverter: NameConverter = SqlObjectMapperUtils.Companion::camelCaseToSnakeCase
): ClassMappingProvider() {

    override fun <T : Any> getClassMappingNonCached(clazz: Class<T>): ClassMapping<T> {
        return GlobalClassInfo(LocalKotlinClassInfo(clazz, nameConverter))
    }
}