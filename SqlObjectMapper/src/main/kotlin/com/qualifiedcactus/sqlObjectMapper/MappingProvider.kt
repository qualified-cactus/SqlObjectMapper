package com.qualifiedcactus.sqlObjectMapper

import com.qualifiedcactus.sqlObjectMapper.fromRs.*
import com.qualifiedcactus.sqlObjectMapper.toParam.DefaultParamClassMapping
import com.qualifiedcactus.sqlObjectMapper.toParam.ParamClassMapping
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

internal object MappingProvider {

    private fun mapRsClassLocal(clazz: KClass<*>): RsClassMapping {
        val constructor = if (clazz.constructors.size == 1) {
            clazz.constructors.first()
        } else {
            clazz.constructors.find { c -> c.hasAnnotation<RsConstructor>() }
                ?: throw SqlObjectMapperException("${clazz} has more than 1 constructors but doesn't " +
                    "specify any constructor with " +
                    "com.qualifiedcactus.sqlObjectMapper.fromRs.RsConstructor")
        }

        return if (constructor.parameters.isEmpty()) {
            BeanRsMapping(clazz, constructor)
        } else {
            ConstructorRsMapping(clazz, constructor)
        }
    }

    private val topMappingCache = ConcurrentHashMap<KClass<*>, RsTopClassMapping>()

    fun mapRsClass(clazz: KClass<*>): RsTopClassMapping {
        val cachedResult = topMappingCache[clazz]
        if (cachedResult != null) {
            return cachedResult
        }
        else {
            val r = RsTopClassMapping(mapRsClassLocal(clazz))
            topMappingCache[clazz] = RsTopClassMapping(mapRsClassLocal(clazz))
            return r
        }
    }

    private val paramMappingCache = ConcurrentHashMap<KClass<*>, ParamClassMapping>()

    fun mapParamClass(clazz: KClass<*>): ParamClassMapping {
        val cachedResult = paramMappingCache[clazz]
        if (cachedResult != null) {
            return cachedResult
        }
        else {
            val r = DefaultParamClassMapping(clazz)
            paramMappingCache[clazz] = r
            return r
        }
    }
}