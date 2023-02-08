/*
 * MIT License
 *
 * Copyright (c) 2023 qualified-cactus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.qualifiedcactus.sqlObjectMapper.toParam

import com.qualifiedcactus.sqlObjectMapper.MappingProvider
import com.qualifiedcactus.sqlObjectMapper.fromRs.CamelCaseToUpperSnakeCaseConverter
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

internal class DefaultParamClassMapping(val clazz: KClass<*>) : ParamClassMapping {

    override val valueExtractors = HashMap<String, ParamClassMapping.Parameter>()
    init {
        clazz.memberProperties.forEach { property ->
            if (property.visibility != KVisibility.PUBLIC) {
                return@forEach
            }
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