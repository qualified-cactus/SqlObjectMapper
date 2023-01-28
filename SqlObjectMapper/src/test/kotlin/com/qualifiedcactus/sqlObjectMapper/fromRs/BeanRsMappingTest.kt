package com.qualifiedcactus.sqlObjectMapper.fromRs

import com.qualifiedcactus.sqlObjectMapper.MappingProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

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
        override fun convert(value: Any?): Any? {
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