package com.qualifiedcactus.sqlObjectMapper

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class NamedParameterQueryTest {


    @Test
    fun test1() {
        val npQuery = "SELECT * FROM a WHERE a = :param_1 AND b = :param_2 AND c = :param_1"
        val query = NamedParameterQuery(npQuery)
        assertEquals("SELECT * FROM a WHERE a = ? AND b = ? AND c = ?", query.translatedQuery)
        val param1 = query.parameterIndexes["PARAM_1"]!!
        assertNotNull(param1)
        assertEquals(2, param1.size)
        assertTrue(param1.contains(1) && param1.contains(3))

        val param2 = query.parameterIndexes["PARAM_2"]!!
        assertNotNull(param2)
        assertEquals(1, param2.size)
        assertTrue(param2.contains(2))
    }
}