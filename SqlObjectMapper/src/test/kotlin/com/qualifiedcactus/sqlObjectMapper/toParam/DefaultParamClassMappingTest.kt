package com.qualifiedcactus.sqlObjectMapper.toParam

import com.qualifiedcactus.sqlObjectMapper.MappingProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

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

    class CustomParamConverter : ParamValueConverter {
        override fun convert(value: Any?, objectCreator: JdbcObjectCreator): Any? {
            return (value as Int) + 1
        }
    }

    @Test
    fun test1() {
        val dtoValue = Dto(1, NestedDto(2,3))

        val paramMapping = MappingProvider.mapParamClass(Dto::class)
        val param4Info = paramMapping.valueExtractors["PARAM_4"]!!
        assertNotNull(param4Info)
        assertInstanceOf(CustomParamConverter::class.java, param4Info.converter)
        assertEquals(1, param4Info.getter(dtoValue))

        val param1Info = paramMapping.valueExtractors["PARAM_1"]!!
        assertNotNull(param1Info)
        assertEquals(2, param1Info.getter(dtoValue))

        val param2Info = paramMapping.valueExtractors["PARAM_2"]!!
        assertNotNull(param2Info)
        assertEquals(3, param2Info.getter(dtoValue))

    }
}