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

package npStatementTest

import sqlObjectMapper.annotationProcessing.bean.BeanMappingProvider
import sqlObjectMapper.annotationProcessing.dataClass.DataClassMappingProvider
import mappedResultSetTest.BeansWithNestedDto
import mappedResultSetTest.KotlinDataClassWithNested
import mappedResultSetTest.schemaForNestedTest
import mappedResultSetTest.schemaForNestedTestSeedData
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import sqlObjectMapper.NpPreparedStatement.Companion.prepareNpStatement
import utils.TestUtils
import java.sql.Connection
import org.junit.jupiter.api.RepeatedTest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NpStatementTest {
    
     companion object {
        lateinit var conn: Connection
        val dataClassMappingProvider = DataClassMappingProvider()
        val beanMappingProvider = BeanMappingProvider()

        @BeforeAll
        @JvmStatic
        fun initConn() {
            conn = TestUtils.createConn()
            conn.createStatement().use { stmt ->
                stmt.execute(schemaForNestedTest)
                stmt.execute(schemaForNestedTestSeedData)
            }
        }

        @AfterAll
        @JvmStatic
        fun closeConn() {
            conn.close()
        }
    }



    @RepeatedTest(2)
    fun testDataClassMapping() {
        conn.prepareNpStatement("""
            SELECT COUNT(1) FROM entity_8 WHERE col_81 = :col_81 AND col_82 = :col_82 AND col_83 = :col_83
        """.trimIndent()).use{ stmt->
            stmt.setParameters(
                KotlinDataClassWithNested.Entity8(
                "2",
                KotlinDataClassWithNested.Entity8Nested1(
                    3,
                    KotlinDataClassWithNested.Entity8Nested2(4)
                )
            ), dataClassMappingProvider)
            stmt.execute();
            stmt.resultSet.use{rs ->
                assertTrue(rs.next())
                assertEquals(1, rs.getInt(1))
            }
        }
    }

    @RepeatedTest(2)
    fun testBeanMapping() {
        conn.prepareNpStatement("""
            SELECT COUNT(1) FROM entity_8 WHERE col_81 = :col_81 AND col_82 = :col_82 AND col_83 = :col_83
        """.trimIndent()).use{ stmt->
            stmt.setParameters(BeansWithNestedDto.Entity8(
                "2",
                BeansWithNestedDto.Entity8Nested1(
                    3,
                    BeansWithNestedDto.Entity8Nested2(4)
                )
            ), beanMappingProvider)
            stmt.execute();
            stmt.resultSet.use{rs ->
                assertTrue(rs.next())
                assertEquals(1, rs.getInt(1))
            }
        }
    }
}