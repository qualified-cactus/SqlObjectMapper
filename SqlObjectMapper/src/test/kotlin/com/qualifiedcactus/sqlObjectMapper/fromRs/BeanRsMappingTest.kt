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

package com.qualifiedcactus.sqlObjectMapper.fromRs

import com.qualifiedcactus.sqlObjectMapper.MappingProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.reflect.KClass
import kotlin.reflect.KType

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class BeanRsMappingTest {

    class Dto1 {
        @RsColumn(name = "MY_COLUMN_1", isId = true, converter = ValueConverter::class)
        lateinit var column1: String
        @RsNested
        lateinit var nestedObject: NestedDto
        lateinit var column3: String
        @RsToMany
        lateinit var toManyCollection: Collection<ToManyDto>
    }

    class NestedDto {
        lateinit var column4: String
        lateinit var column5: String
    }

    class ToManyDto {
        lateinit var column6: String
        lateinit var column7: String
    }

    class ValueConverter : RsValueConverter {
        override fun convert(value: Any?, propertyType: KClass<*>): Any? {
            return value
        }
    }

    @Test
    fun test1() {
        val clazzMapping = MappingProvider.mapRsClass(Dto1::class).rootMapping
        Assertions.assertInstanceOf(BeanRsMapping::class.java, clazzMapping)
        Assertions.assertEquals(Dto1::class, clazzMapping.clazz)
        Assertions.assertEquals(
            clazzMapping.properties.size,
            clazzMapping.simpleProperties.size
                + clazzMapping.nestedProperties.size
                + clazzMapping.toManyProperties.size
        )

        //----------------------------

        val property1 = clazzMapping.simpleProperties.find {
            it.name == "MY_COLUMN_1"
        }
        Assertions.assertNotNull(property1)
        Assertions.assertTrue(property1!!.isId)
        Assertions.assertEquals(ValueConverter::class, property1.valueConverter::class)

        //----------------------------

        val property3 = clazzMapping.simpleProperties.find {
            it.name == "COLUMN_3"
        }
        Assertions.assertNotNull(property3)
        Assertions.assertFalse(property3!!.isId)
        Assertions.assertEquals(RsNoOpConverter::class, property3.valueConverter::class)

        //----------------------------

        val property2 = clazzMapping.nestedProperties.first()
        Assertions.assertEquals(NestedDto::class, property2.classMapping.rootMapping.clazz)
        Assertions.assertEquals(2, property2.classMapping.rootMapping.simpleProperties.size)
        Assertions.assertEquals(
            property2.classMapping.rootMapping.properties.size,
            property2.classMapping.rootMapping.simpleProperties.size
        )

        Assertions.assertNotNull(property2.classMapping.rootMapping.simpleProperties.find { it.name == "COLUMN_4" })
        Assertions.assertNotNull(property2.classMapping.rootMapping.simpleProperties.find { it.name == "COLUMN_5" })

        //----------------------------

        val property4 = clazzMapping.toManyProperties.first()
        Assertions.assertEquals(ToManyDto::class, property4.elementMapping.rootMapping.clazz)
        Assertions.assertEquals(Collection::class, property4.collectionType)
        Assertions.assertEquals(RsNoOpConverter::class, property4.valueConverter::class)
    }
}