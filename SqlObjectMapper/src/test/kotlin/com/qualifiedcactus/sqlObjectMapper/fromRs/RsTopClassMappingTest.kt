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