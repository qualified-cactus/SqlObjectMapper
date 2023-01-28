package com.qualifiedcactus.sqlObjectMapper.toParam

import com.qualifiedcactus.sqlObjectMapper.MappingProvider
import com.qualifiedcactus.sqlObjectMapper.fromRs.CamelCaseToUpperSnakeCaseConverter
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

internal class DefaultParamClassMapping(val clazz: KClass<*>) : ParamClassMapping {

    override val valueExtractors = HashMap<String, ParamClassMapping.Parameter>()
    init {
        clazz.memberProperties.forEach { property ->
            val field = property.javaField
            if (field != null) {
                val ignoreParam = field.getAnnotation(IgnoreParam::class.java)
                if (ignoreParam != null) {
                    return@forEach
                }
                val param = field.getAnnotation(Param::class.java)
                val nestedParams = field.getAnnotation(NestedParams::class.java)

                if (param != null) {
                    val name = if (param.name == "") {
                        CamelCaseToUpperSnakeCaseConverter.convert(property.name)
                    } else {
                        param.name.uppercase()
                    }

                    valueExtractors[name] = ParamClassMapping.Parameter(
                        {o -> property.getter.call(o)},
                        param.converter.createInstance()
                    )
                }
                else if (nestedParams != null) {
                    val nested = MappingProvider.mapParamClass(property.returnType.classifier as KClass<*>)
                    nested.valueExtractors.forEach {(paramName, parameterInfo)->
                        valueExtractors[paramName] = ParamClassMapping.Parameter(
                            {o -> parameterInfo.getter(property.getter.call(o)!!) },
                            parameterInfo.converter
                        )
                    }
                }
                else {
                    valueExtractors[
                        CamelCaseToUpperSnakeCaseConverter.convert(property.name)
                    ] = ParamClassMapping.Parameter(
                        {o -> property.getter.call(o)},
                        ParamNoOpConverter()
                    )
                }
            }
        }
    }

}