/*
 MIT License

 Copyright (c) 2022 qualified-cactus

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package example

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import sqlObjectMapper.ClassMappingProvider
import sqlObjectMapper.MappedResultSet
import sqlObjectMapper.NpPreparedStatement.Companion.prepareNpStatement
import sqlObjectMapper.QueryExecutor
import sqlObjectMapper.TransactionManager
import sqlObjectMapper.annotationProcessing.dataClass.DataClassMappingProvider
import utils.TestUtils
import java.sql.Connection


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KotlinExampleTest {

    companion object {

        lateinit var connection: Connection

        @BeforeAll
        @JvmStatic
        fun initDb() {
            ClassMappingProvider.defaultClassMappingProvider = DataClassMappingProvider()
            connection = TestUtils.createConn()
            connection.createStatement().use {stmt->
                stmt.execute("""
                CREATE TABLE entity_1 (
                    column_1 INTEGER PRIMARY KEY,
                    column_2 INTEGER,
                    column_3 INTEGER
                );
                """
                )
                stmt.execute("""
                INSERT INTO entity_1 VALUES (1,2,3);
                INSERT INTO entity_1 VALUES (2,3,4);
                INSERT INTO entity_1 VALUES (3,4,5);
                """
                )
            }
        }

        @AfterAll
        @JvmStatic
        fun closeDb() {
            connection.close()
        }
    }

    data class Entity1(
        val column1: Int,
        val column2: Int,
        val column3: Int
    )
    data class QueryInput(
        val param1: Int
    )
    @Test
    fun example1() {
        val queriedEntity: Entity1? = connection
            .prepareNpStatement("SELECT * FROM entity_1 WHERE column_2 = :param_1")
            .use { stmt ->
                stmt.setParameters(QueryInput(3))
                stmt.execute()
                MappedResultSet(stmt.resultSet).toObject(Entity1::class.java)
            }
        Assertions.assertEquals(2, queriedEntity?.column1)
    }

    @Test
    fun example2() {
        val queriedEntity: Entity1? = QueryExecutor()
            .queryForObject(connection,
                "SELECT * FROM entity_1 WHERE column_2 = :param_1",
                QueryInput(3),
                Entity1::class.java
            )
        Assertions.assertEquals(2, queriedEntity?.column1)
    }

    @Test
    fun example3() {
        TransactionManager.executeTransaction(connection) { conn ->
            val queriedEntity: Entity1? = QueryExecutor()
                .queryForObject(conn,
                    "SELECT * FROM entity_1 WHERE column_2 = :param_1",
                    QueryInput(3),
                    Entity1::class.java
                )
            Assertions.assertEquals(2, queriedEntity?.column1)
        }
    }
}