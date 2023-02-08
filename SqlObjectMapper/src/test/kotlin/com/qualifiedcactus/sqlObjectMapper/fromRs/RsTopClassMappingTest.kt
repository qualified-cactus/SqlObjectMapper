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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RsTopClassMappingTest {

    class Dto1 {
        @RsColumn(isId = true)
        lateinit var idColumn1: String
        @RsNested
        lateinit var nested1: Dto2
        @RsToMany
        lateinit var toMany: Collection<Dto3>
    }
    class Dto2(
        @RsColumn(isId = true)
        val idColumn2: String,
        @RsToMany
        val toMany: Collection<Dto4>
    )
    class Dto3 (
        @RsColumn(isId = true)
        val idColumn3: String
    )
    class Dto4(
        @RsColumn(isId = true)
        val idColumn4: String
    )

    class SimpleDto(
        val column1: String,
        val column2: String,
        val column3: String,
    )

    @Test
    fun testSimpleDto() {
        val clazzMapping = MappingProvider.mapRsClass(SimpleDto::class)
        assertTrue(clazzMapping.isSimple)
        assertTrue(clazzMapping.idMapping.noId)
        assertTrue(clazzMapping.propertyNameDict.containsKey("COLUMN_1"))
        assertTrue(clazzMapping.propertyNameDict.containsKey("COLUMN_2"))
        assertTrue(clazzMapping.propertyNameDict.containsKey("COLUMN_3"))
        assertEquals(3, clazzMapping.propertyNameDict.size)
    }

    @Test
    fun testComplexDto() {
        val clazzMapping = MappingProvider.mapRsClass(Dto1::class)
        assertFalse(clazzMapping.isSimple)

        assertFalse(clazzMapping.idMapping.noId)
        assertEquals(2, clazzMapping.idMapping.idColumnNames.size)
        assertTrue(clazzMapping.idMapping.idColumnNames.contains("ID_COLUMN_1"))
        assertTrue(clazzMapping.idMapping.idColumnNames.contains("ID_COLUMN_2"))

        assertEquals(2, clazzMapping.toManyList.size)
        assertNotNull(clazzMapping.toManyList.find { it.elementMapping.rootMapping.clazz == Dto3::class})
        assertNotNull(clazzMapping.toManyList.find { it.elementMapping.rootMapping.clazz == Dto4::class})

    }
}