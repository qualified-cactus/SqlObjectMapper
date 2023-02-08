package com.qualifiedcactus.sqlObjectMapper.fromRs

import com.qualifiedcactus.sqlObjectMapper.MappingProvider
import com.qualifiedcactus.sqlObjectMapper.SqlObjectMapperException
import com.qualifiedcactus.sqlObjectMapper.fromRs.RsClassMapping.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

internal class BeanRsMapping(
    override val clazz: KClass<*>,
    val constructor: KFunction<Any>
): RsClassMapping {

    override val properties = ArrayList<ClassProperty>()
    override val simpleProperties = ArrayList<SimpleProperty>()
    override val nestedProperties = ArrayList<NestedProperty>()
    override val toManyProperties = ArrayList<ToManyProperty>()

    private val propertiesToSet = ArrayList<KMutableProperty1<out Any, *>>()
    init {

        clazz.memberProperties.forEach {property ->
            val field = property.javaField
            if (property is KMutableProperty1 && field != null && property.visibility == KVisibility.PUBLIC) {
                val rsIgnore = field.getAnnotation(RsIgnore::class.java)
                if (rsIgnore != null) {
                    return@forEach
                }

                val rsColumn = field.getAnnotation(RsColumn::class.java)
                val rsToOne = field.getAnnotation(RsToOne::class.java)
                val rsNested = field.getAnnotation(RsNested::class.java)
                val rsToMany = field.getAnnotation(RsToMany::class.java)

                val propertyInfo: ClassProperty = if (rsColumn != null) {
                    SimpleProperty(
                        if (rsColumn.name != "")
                            rsColumn.name
                        else
                            CamelCaseToUpperSnakeCaseConverter.convert(property.name)
                        ,
                        rsColumn.isId,
                        rsColumn.converter.createInstance()
                    ).also(simpleProperties::add)
                }
                else if (rsToOne != null) {
                    NestedProperty(
                        MappingProvider.mapRsClass(property.returnType.classifier as KClass<*>),
                        true
                    ).also(nestedProperties::add)
                }
                else if (rsNested != null) {
                    NestedProperty(
                        MappingProvider.mapRsClass(property.returnType.classifier as KClass<*>),
                        false
                    ).also(nestedProperties::add)
                }
                else if (rsToMany != null) {
                    val classToMap = if (rsToMany.classToMap == Any::class) {
                        (property.returnType.arguments.getOrNull(0)?.type
                            ?: throw SqlObjectMapperException("Invalid to many type in ${clazz}"))
                            .classifier as KClass<*>
                    } else {
                        rsToMany.classToMap
                    }

                    ToManyProperty(
                        property.returnType.classifier as KClass<*>,
                        MappingProvider.mapRsClass(classToMap),
                        rsToMany.elementConverter.createInstance()
                    ).also(toManyProperties::add)
                }
                else {
                    SimpleProperty(
                        CamelCaseToUpperSnakeCaseConverter.convert(property.name),
                        false,
                        RsNoOpConverter()
                    ).also(simpleProperties::add)
                }

                properties.add(propertyInfo)
                propertiesToSet.add(property)
            }
        }
    }

    override fun createInstance(properties: Array<Any?>): Any {
        val instance = constructor.call()
        propertiesToSet.forEachIndexed { i, property ->
            println("method: ${property.name} ${property.setter.name} ${property.getter.name}")
            property.setter.call(instance, properties[i])
        }
        return instance
    }

}