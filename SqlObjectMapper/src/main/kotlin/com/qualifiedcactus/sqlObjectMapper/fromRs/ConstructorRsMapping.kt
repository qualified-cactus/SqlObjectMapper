package com.qualifiedcactus.sqlObjectMapper.fromRs

import com.qualifiedcactus.sqlObjectMapper.MappingProvider
import com.qualifiedcactus.sqlObjectMapper.SqlObjectMapperException
import com.qualifiedcactus.sqlObjectMapper.fromRs.RsClassMapping.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

internal class ConstructorRsMapping(
    override val clazz: KClass<*>,
    val constructor: KFunction<Any>
) : RsClassMapping {

    override val properties = ArrayList<ClassProperty>()
    override val simpleProperties = ArrayList<SimpleProperty>()
    override val nestedProperties = ArrayList<NestedProperty>()
    override val toManyProperties = ArrayList<ToManyProperty>()

    init {
        constructor.parameters.forEach { parameter ->
            val rsColumn = parameter.findAnnotation<RsColumn>()
            val rsToOne = parameter.findAnnotation<RsToOne>()
            val rsNested = parameter.findAnnotation<RsNested>()
            val rsToMany = parameter.findAnnotation<RsToMany>()

            val name = parameter.name
                ?: throw SqlObjectMapperException("Parameter name is not available in runtime")
            val propertyInfo: ClassProperty = if (rsColumn != null) {
                SimpleProperty(
                    if (rsColumn.name != "")
                        rsColumn.name
                    else
                        CamelCaseToUpperSnakeCaseConverter.convert(name)
                    ,
                    rsColumn.isId,
                    rsColumn.converter.createInstance()
                ).also(simpleProperties::add)
            }
            else if (rsToOne != null) {
                NestedProperty(
                    MappingProvider.mapRsClass(parameter.type.classifier as KClass<*>),
                    true
                )
            }
            else if (rsNested != null) {
                NestedProperty(
                    MappingProvider.mapRsClass(parameter.type.classifier as KClass<*>),
                    false
                ).also(nestedProperties::add)
            }
            else if (rsToMany != null) {
                val classToMap: KClass<*> = if (rsToMany.classToMap == Any::class) {
                    (parameter.type.arguments.getOrNull(0)?.type
                        ?: throw SqlObjectMapperException("Invalid to many type in ${clazz}"))
                        .classifier as KClass<*>
                }
                else {
                    rsToMany.classToMap
                }
                ToManyProperty(
                    parameter.type.classifier as KClass<*>,
                    MappingProvider.mapRsClass(classToMap),
                    rsToMany.elementConverter.createInstance()
                ).also(toManyProperties::add)
            }
            else {
                SimpleProperty(
                    CamelCaseToUpperSnakeCaseConverter.convert(name),
                    false,
                    RsNoOpConverter()
                ).also(simpleProperties::add)
            }
            properties.add(propertyInfo)
        }
    }

    override fun createInstance(properties: Array<Any?>): Any {
        return constructor.call(*properties)
    }
}