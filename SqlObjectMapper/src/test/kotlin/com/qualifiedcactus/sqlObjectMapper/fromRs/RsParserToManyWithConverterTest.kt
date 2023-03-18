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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.*
import kotlin.reflect.KClass

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RsParserToManyWithConverterTest {

    class MyParentDto(
        @RsColumn(isId = true)
        val id: Long,
        @RsToMany(classToMap = MyChildDto::class, elementConverter = ChildToStringConverter::class)
        val childNames: List<String>
    )

    class MyChildDto(
        @RsColumn(isId = true)
        val childId: Long,
        val childName: String?
    )

    class ChildToStringConverter : RsElementConverter {
        override fun convert(value: Any?, propertyType: KClass<*>): Any? {
            if (value is MyChildDto) {
                return value.childName
            } else throw RuntimeException("invalid type")
        }

    }


    val table = """
        CREATE TABLE table_a (
            id BIGINT PRIMARY KEY
        );
        
        CREATE TABLE table_b (
            child_id BIGINT PRIMARY KEY,
            child_name VARCHAR(50),
            parent_id BIGINT NOT NULL REFERENCES table_a (id) 
        );
    """.trimIndent()
    val data = """
        INSERT INTO table_a VALUES (1);
        INSERT INTO table_a VALUES (2);
        INSERT INTO table_a VALUES (3);
        
        INSERT INTO table_b VALUES (1, 'foo', 2);
        INSERT INTO table_b VALUES (2, null, 2);
        INSERT INTO table_b VALUES (3, 'bazz', 2);
        
        INSERT INTO table_b VALUES (4, 'bar', 1);
        INSERT INTO table_b VALUES (5, 'ben', 3);
    """.trimIndent()
    val connection = createDatabaseConnection()

    init {
        connection.createStatement().use {
            it.execute(table)
            it.execute(data)
        }
    }

    @Test
    fun test1() {
        val sqlQuery = """
            SELECT * 
            FROM table_a a
            LEFT JOIN table_b b ON a.id = b.parent_id
            ORDER BY a.id ASC, b.child_id ASC
        """.trimIndent()

        val result = connection.createStatement().use {
            it.executeQuery(sqlQuery).toList(MyParentDto::class)
        }
        assertEquals(3, result.size)
        assertEquals(1, result[0].id)
        assertEquals(2, result[1].id)
        assertEquals(3, result[2].id)

        assertEquals(1, result[0].childNames.size)
        assertEquals(3, result[1].childNames.size)
        assertEquals(1, result[2].childNames.size)

        assertEquals("bar", result[0].childNames[0])

        assertEquals("foo", result[1].childNames[0])
        assertNull(result[1].childNames[1])
        assertEquals("bazz", result[1].childNames[2])

        assertEquals("ben", result[2].childNames[0])
    }
}