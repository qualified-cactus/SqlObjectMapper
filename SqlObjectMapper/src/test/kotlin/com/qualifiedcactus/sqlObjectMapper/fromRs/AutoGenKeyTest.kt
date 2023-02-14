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

import com.qualifiedcactus.sqlObjectMapper.createDatabaseConnection
import com.qualifiedcactus.sqlObjectMapper.prepareNpStatement
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutoGenKeyTest : AutoCloseable{

    val connection = createDatabaseConnection()

    @AfterAll
    override fun close() {
        connection.close()
    }

    val table = """
        CREATE SEQUENCE seq_1 START WITH 1
        CREATE SEQUENCE seq_2 START WITH 10
        CREATE SEQUENCE seq_3 START WITH 20
        
        CREATE TABLE table_1(
            column_1 INTEGER PRIMARY KEY,
        )
        
        CREATE TABLE table_2 (
            column_1 INTEGER PRIMARY KEY,
            column_2 INTEGER NOT NULL
        )
        
    """.trimIndent()
    init {
        connection.createStatement().use {
            it.execute(table)
        }
    }
    class CompositeKeyDto(
        val column1: Int,
        @RsNested
        val nested: NestedDto
    )
    class NestedDto(
        val column2: Int
    )

    @Test
    fun testSingleKey() {
        val declaredKey1 = SingleAutoGenKey("column_1", Int::class)

        val stmt = connection.prepareNpStatement(
            "INSERT INTO table_1 VALUES (NEXT VALUE FOR seq_1)",
            declaredKey1
        )

        assertEquals(1, stmt.executeUpdateWithGeneratedKeys(declaredKey1).first())
        assertEquals(2, stmt.executeUpdateWithGeneratedKeys(declaredKey1).first())
        assertEquals(3, stmt.executeUpdateWithGeneratedKeys(declaredKey1).first())
    }

    @Test
    fun testCompositeKey() {
        val declaredKey2 = CompositeAutoGenKey(CompositeKeyDto::class)
        val stmt = connection.prepareNpStatement(
            "INSERT INTO table_2 VALUES (NEXT VALUE FOR seq_2, NEXT VALUE FOR seq_3)",
            declaredKey2
        )
        stmt.executeUpdateWithGeneratedKeys(declaredKey2).also {
            assertEquals(10, it.first().column1)
            assertEquals(20, it.first().nested.column2)
        }
        stmt.executeUpdateWithGeneratedKeys(declaredKey2).also {
            assertEquals(11, it.first().column1)
            assertEquals(21, it.first().nested.column2)
        }
        stmt.executeUpdateWithGeneratedKeys(declaredKey2).also {
            assertEquals(12, it.first().column1)
            assertEquals(22, it.first().nested.column2)
        }
    }

}