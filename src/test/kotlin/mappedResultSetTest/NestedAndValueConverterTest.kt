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

package mappedResultSetTest

import annotationProcessing.JdbcObjectCreator
import javaImpl.BeanMappingProvider
import kotlinImpl.DataClassMappingProvider
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.TestInstance
import sqlObjectMapper.MappedResultSet
import sqlObjectMapper.ValueConverter
import utils.TestUtils
import java.sql.Connection


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NestedAndValueConverterTest {
    
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
        conn.createStatement().use { stmt ->
            MappedResultSet(stmt.executeQuery(testQuery3), dataClassMappingProvider).use { rs ->
                val results = rs.toList(KotlinDataClassWithNested.Entity8::class.java)
                testResults(results)
            }
        }
    }

    @RepeatedTest(2)
    fun testBeanMapping() {
        conn.createStatement().use { stmt ->
            MappedResultSet(stmt.executeQuery(testQuery3), beanMappingProvider).use { rs ->
                val results = rs.toList(BeansWithNestedDto.Entity8::class.java)
                testResults(results)
            }
        }
    }

    private fun testResults(results: List<IEntity8>) {
        assertEquals(3, results.size)

        assertEquals("1", results[0].col81)
        assertEquals(2, results[0].nested!!.col82)
        assertEquals(3, results[0].nested!!.nested!!.col83)

        assertEquals("2", results[1].col81)
        assertEquals(3, results[1].nested!!.col82)
        assertEquals(4, results[1].nested!!.nested!!.col83)

        assertEquals("3", results[2].col81)
        assertEquals(4, results[2].nested!!.col82)
        assertEquals(5, results[2].nested!!.nested!!.col83)
    }
}


class IntToStringValueConverter : ValueConverter() {

    override fun toDb(jdbcObjectCreator: JdbcObjectCreator, value: Any?): Int {
        return Integer.parseInt(value as String)
    }

    override fun fromDb(value: Any?): String {
        return (value as Int).toString();
    }
}