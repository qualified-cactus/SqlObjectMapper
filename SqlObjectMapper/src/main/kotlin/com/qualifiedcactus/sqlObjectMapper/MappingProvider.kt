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

package com.qualifiedcactus.sqlObjectMapper

import com.qualifiedcactus.sqlObjectMapper.fromRs.*
import com.qualifiedcactus.sqlObjectMapper.toParam.DefaultParamClassMapping
import com.qualifiedcactus.sqlObjectMapper.toParam.ParamClassMapping
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.hasAnnotation

internal object MappingProvider {

    private fun mapRsClassLocal(clazz: KClass<*>): RsClassMapping {
        val availableConstructors = clazz.constructors.filter { it.visibility == KVisibility.PUBLIC }
        val constructor = if (availableConstructors.size == 1) {
            availableConstructors.first()
        } else {
            availableConstructors.find { c -> c.hasAnnotation<RsConstructor>() }
                ?: throw SqlObjectMapperException("${clazz} has more than 1 constructors but doesn't " +
                    "specify any constructor with " + RsConstructor::class.qualifiedName)
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