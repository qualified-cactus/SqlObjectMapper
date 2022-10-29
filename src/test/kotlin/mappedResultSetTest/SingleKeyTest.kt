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

import sqlObjectMapper.annotationProcessing.bean.BeanMappingProvider
import sqlObjectMapper.annotationProcessing.dataClass.DataClassMappingProvider
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.TestInstance
import sqlObjectMapper.MappedResultSet
import utils.TestUtils
import java.sql.Connection


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingleKeyTest {

    companion object {
        lateinit var conn: Connection
        val dataClassMappingProvider = DataClassMappingProvider()
        val beanMappingProvider = BeanMappingProvider()

        @BeforeAll
        @JvmStatic
        fun initConn() {
            conn = TestUtils.createConn()
            conn.createStatement().use { stmt ->
                stmt.execute(schemaWithSingleKey)
                stmt.execute(schemaWithSingleKeySeedData)
            }
        }

        @AfterAll
        @JvmStatic
        fun closeConn() {
            conn.close()
        }
    }

    @RepeatedTest(2)
    fun dataClassMappingTest() {
        conn.createStatement().use { stmt->
            stmt.executeQuery(testQuery2).use { rs->
                val results = MappedResultSet(rs, dataClassMappingProvider)
                    .toList(KotlinDataClassDtoSingleKey.Entity4::class.java)

                testResults(results)
            }
        }
    }

    @RepeatedTest(2)
    fun beanMappingTest() {
        conn.createStatement().use { stmt->
            stmt.executeQuery(testQuery2).use { rs->
                val results = MappedResultSet(rs, beanMappingProvider)
                    .toList(BeansWithSingleKeyDto.Entity4::class.java)

                testResults(results)
            }
        }
    }

    fun testResults(results: List<IEntity4>) {
        assertEquals(3, results.size)

        results[0].apply {
            assertEquals(1, this.col41)
            assertEquals(2, this.e5List!!.size)
            this.e5List!!.let { l ->

                assertEquals(1, l[0].col51)
                assertEquals(0, l[0].e6List!!.size)
                assertEquals(0, l[0].e7List!!.size)


                assertEquals(2, l[1].col51)

                assertEquals(3, l[1].e6List!!.size)
                assertEquals(1, l[1].e6List!![0].col61)
                assertEquals(2, l[1].e6List!![1].col61)
                assertEquals(3, l[1].e6List!![2].col61)

                assertEquals(3, l[1].e7List!!.size)
                assertEquals(1, l[1].e7List!![0].col71)
                assertEquals(2, l[1].e7List!![1].col71)
                assertEquals(3, l[1].e7List!![2].col71)
            }

            results[1].apply {
                assertEquals(2, this.col41)
                assertEquals(0, this.e5List!!.size)
            }

            results[2].apply {
                assertEquals(3, this.col41)
                assertEquals(1, this.e5List!!.size)

                this.e5List!!.let {
                    assertEquals(3, it[0].col51)
                    assertEquals(0, it[0].e6List!!.size)
                    assertEquals(0, it[0].e7List!!.size)
                }
            }

        }





        assertEquals(2, results[1].col41)
        assertEquals(3, results[2].col41)

    }

}