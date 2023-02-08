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