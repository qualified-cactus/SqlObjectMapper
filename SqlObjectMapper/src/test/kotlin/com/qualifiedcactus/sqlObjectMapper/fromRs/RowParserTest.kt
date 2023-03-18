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
import com.qualifiedcactus.sqlObjectMapper.createDatabaseConnection
import com.qualifiedcactus.sqlObjectMapper.fromRs.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import java.sql.Connection
import kotlin.reflect.KClass

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RowParserTest : AutoCloseable {

    val tableSql = """
        
        CREATE TABLE table_a (
            column_1 INTEGER PRIMARY KEY,
            column_2 INTEGER NOT NULL
        );
        
        CREATE TABLE table_b (
            column_3 INTEGER PRIMARY KEY,
            column_4 INTEGER NOT NULL REFERENCES table_a(column_1)
        );
        
    """

    val dataSql = """
        INSERT INTO table_a VALUES (1,2);
        INSERT INTO table_a VALUES (3,4);
        
        INSERT INTO table_b VALUES (1,1);
        INSERT INTO table_b VALUES (2,1);
    """.trimIndent()



    val connection: Connection = createDatabaseConnection()
    init {
        connection.createStatement().use { stmt ->
            stmt.execute(tableSql)
            stmt.execute(dataSql)
        }
    }

    @AfterAll
    override fun close() {
        connection.close()
    }


    class SimpleDto1(
        @RsColumn(converter = IncrementConverter::class)
        val column1: Int,
        val column2: Int
    )

    class IncrementConverter : RsElementConverter {
        override fun convert(value: Any?, propertyType: KClass<*>): Any? {
            return requireNotNull(value) as Int + 10
        }

    }

    @Test
    fun simpleParseTest() {
        val dto = connection.createStatement().use { stmt ->
            stmt.executeQuery("SELECT * FROM table_a WHERE column_1 = 1").use {rs ->
                assertTrue(rs.next())
                val rowParser = RowParser(rs)
                rowParser.simpleParse(MappingProvider.mapRsClass(SimpleDto1::class)) as SimpleDto1
            }
        }
        assertEquals(11, dto.column1)
        assertEquals(2, dto.column2)
    }


    class ComplexDto(
        @RsNested
        val nested: NestedDto,
        @RsToMany
        val toMany: Collection<ToManyDto>
    )
    class NestedDto(
        @RsColumn(isId = true)
        val column1: Int
    )
    class ToManyDto(
        @RsColumn(isId = true)
        val column3: Int
    )

    @Test
    fun parseWithNestedTest() {
        val query = """
            SELECT a.column_1, b.column_3 
            FROM table_a a
            LEFT JOIN table_b b ON a.column_1 = b.column_4
            WHERE a.column_1 = 1
        """.trimIndent()
        val parsedResult = connection.createStatement().use { stmt ->
            stmt.executeQuery(query).use {rs ->
                assertTrue(rs.next())
                val rowParser = RowParser(rs)
                rowParser.parseWithNested(MappingProvider.mapRsClass(ComplexDto::class))
            }
        }
        val dto = parsedResult.value as ComplexDto

        assertEquals(1, dto.nested.column1)
        assertInstanceOf(List::class.java, dto.toMany)
        assertEquals(1, parsedResult.toManyCollectionList.size)
        assertEquals(ToManyDto::class, parsedResult.toManyCollectionList[0].info.elementMapping.rootMapping.clazz)
    }


}