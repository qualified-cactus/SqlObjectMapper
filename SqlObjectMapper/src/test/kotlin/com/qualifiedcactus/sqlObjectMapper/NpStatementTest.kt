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

import com.qualifiedcactus.sqlObjectMapper.fromRs.toList
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.sql.Connection

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class NpStatementTest : AutoCloseable {

    val createTable = """
        CREATE TABLE table_a (
            column_1 VARCHAR(50) NOT NULL PRIMARY KEY,
            column_2 VARCHAR(50) NOT NULL
        )
    """.trimIndent()

    val data = """
        INSERT INTO table_a VALUES ('foo', 'bar');
        INSERT INTO table_a VALUES ('bar', 'bazz');
    """.trimIndent()

    val connection: Connection = createDatabaseConnection()

    init {
        connection.createStatement().use {
            it.execute(createTable)
            it.execute(data)
        }
    }

    class InDto(
        val column1: String
    )

    class OutDto(
        val column1: String,
        val column2: String
    )

    @Test
    fun test1() {
        val results = connection
            .prepareNpStatement(
                "SELECT * FROM table_a WHERE column_1 = :column_1"
            )
            .setParametersByDto(InDto("bar"))
            .useExecuteQuery { it.toList(OutDto::class) }
        assertEquals(1, results.size)
        assertEquals("bazz", results.first().column2)

    }

    @AfterAll
    override fun close() {
       connection.close()
    }


}