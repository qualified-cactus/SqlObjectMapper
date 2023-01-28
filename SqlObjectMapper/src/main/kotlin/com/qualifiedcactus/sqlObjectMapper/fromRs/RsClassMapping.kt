package com.qualifiedcactus.sqlObjectMapper.fromRs

import kotlin.reflect.KClass

internal interface RsClassMapping {
    val clazz: KClass<*>

    val properties: List<ClassProperty>
    fun createInstance(properties: Array<Any?>): Any

    val simpleProperties: List<SimpleProperty>
    val nestedProperties: List<NestedProperty>
    val toManyProperties: List<ToManyProperty>

    interface ClassProperty

    class SimpleProperty(
        val name: String,
        val isId: Boolean,
        val valueConverter: RsValueConverter
    ) : ClassProperty

    class NestedProperty(
        val classMapping: RsTopClassMapping,
        val toOne: Boolean
    ) : ClassProperty

    class ToManyProperty(
        val collectionType: KClass<*>,
        val elementMapping: RsTopClassMapping,
        val valueConverter: RsValueConverter
    ) : ClassProperty

}



