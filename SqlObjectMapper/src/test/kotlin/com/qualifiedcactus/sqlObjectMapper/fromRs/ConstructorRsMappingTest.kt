package com.qualifiedcactus.sqlObjectMapper.fromRs

import com.qualifiedcactus.sqlObjectMapper.*
import com.qualifiedcactus.sqlObjectMapper.fromRs.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ConstructorRsMappingTest {


    class Dto1(
        @RsColumn(name = "MY_COLUMN_1", isId = true, converter = ValueConverter::class)
        val column1: String,
        @RsNested
        val nestedObject: NestedDto,
        val column3: String,
        @RsToMany
        val toManyCollection: Collection<ToManyDto>
    )

    class NestedDto(
        val column4: String,
        val column5: String
    )

    class ToManyDto(
        val column6: String,
        val column7: String
    )
    class ValueConverter : RsValueConverter {
        override fun convert(value: Any?): Any? {
            return value
        }
    }

    @Test
    fun test1() {
        val clazzMapping = MappingProvider.mapRsClass(Dto1::class).rootMapping
        assertInstanceOf(ConstructorRsMapping::class.java, clazzMapping)
        assertEquals(Dto1::class, clazzMapping.clazz)
        assertEquals(
            clazzMapping.properties.size,
            clazzMapping.simpleProperties.size
                + clazzMapping.nestedProperties.size
                + clazzMapping.toManyProperties.size
        )
        
        //----------------------------

        val property1 = clazzMapping.simpleProperties.find {
            it.name == "MY_COLUMN_1"
        }
        assertNotNull(property1)
        assertTrue(property1!!.isId)
        assertEquals(ValueConverter::class, property1.valueConverter::class)

        //----------------------------

        val property3 = clazzMapping.simpleProperties.find {
            it.name == "COLUMN_3"
        }
        assertNotNull(property3)
        assertFalse(property3!!.isId)
        assertEquals(RsNoOpConverter::class, property3.valueConverter::class)

        //----------------------------

        val property2 = clazzMapping.nestedProperties.first()
        assertEquals(NestedDto::class, property2.classMapping.rootMapping.clazz)
        assertEquals(2, property2.classMapping.rootMapping.simpleProperties.size)
        assertEquals(property2.classMapping.rootMapping.properties.size, property2.classMapping.rootMapping.simpleProperties.size)

        assertNotNull(property2.classMapping.rootMapping.simpleProperties.find{it.name == "COLUMN_4"})
        assertNotNull(property2.classMapping.rootMapping.simpleProperties.find{it.name == "COLUMN_5"})

        //----------------------------

        val property4 = clazzMapping.toManyProperties.first()
        assertEquals(ToManyDto::class, property4.elementMapping.rootMapping.clazz)
        assertEquals(Collection::class, property4.collectionType)
        assertEquals(RsNoOpConverter::class, property4.valueConverter::class)
    }

}