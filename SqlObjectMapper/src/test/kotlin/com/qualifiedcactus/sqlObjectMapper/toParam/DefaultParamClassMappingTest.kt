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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.sql.PreparedStatement
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KType

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DefaultParamClassMappingTest {

    class Dto(
        @Param("param_4", CustomParamConverter::class)
        val param3: Int,
        @NestedParams
        val nested: NestedDto
    )

    class NestedDto(
        val param1: Int,
        val param2: Int
    )

    class CustomParamConverter(kType: KType, annotation: KAnnotatedElement) : ParamValueSetter(kType, annotation) {
        override fun processValue(propertyValue: Any?, objectCreator: JdbcObjectCreator): Any? {
            return (propertyValue as Int) + 1
        }
    }

    @Test
    fun test1() {
        val dtoValue = Dto(1, NestedDto(2,3))

        val paramMapping = MappingProvider.mapParamClass(Dto::class)
        val param4Info = paramMapping.parametersNameMap["PARAM_4"]!!
        assertNotNull(param4Info)
        assertInstanceOf(CustomParamConverter::class.java, param4Info.extractor)
        assertEquals(1, param4Info.getter(dtoValue))

        val param1Info = paramMapping.parametersNameMap["PARAM_1"]!!
        assertNotNull(param1Info)
        assertEquals(2, param1Info.getter(dtoValue))

        val param2Info = paramMapping.parametersNameMap["PARAM_2"]!!
        assertNotNull(param2Info)
        assertEquals(3, param2Info.getter(dtoValue))

    }
}