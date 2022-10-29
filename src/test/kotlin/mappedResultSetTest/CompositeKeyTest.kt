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
import utils.TestUtils.Companion.createConn
import sqlObjectMapper.annotationProcessing.dataClass.DataClassMappingProvider
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.TestInstance
import sqlObjectMapper.MappedResultSet
import java.sql.Connection


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompositeKeyTest {

    companion object {
        lateinit var conn: Connection
        val dataClassMappingProvider = DataClassMappingProvider()
        val beanMappingProvider = BeanMappingProvider()

        @BeforeAll
        @JvmStatic
        fun initConn() {
            conn = createConn()
            conn.createStatement().use { stmt ->
                stmt.execute(schemaWithCompositeKey)
                stmt.execute(schemaWithCompositeKeySeedData)
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

        conn.createStatement().use { stmt->
            stmt.executeQuery(testQuery1).use { rs->
                val results = MappedResultSet(rs, dataClassMappingProvider)
                    .toList(KotlinDataClassDtoCompositeKey.Entity1::class.java)

                testResults(results)
            }
        }
    }

    @RepeatedTest(2)
    fun testBeanMapping() {
        conn.createStatement().use { stmt->
            stmt.executeQuery(testQuery1).use { rs->
                val results = MappedResultSet(rs, beanMappingProvider)
                    .toList(BeansWithCompositeKeyDto.Entity1::class.java)

                testResults(results)
            }
        }
    }


    private fun testResults(results: List<IEntity1>) {
        assertEquals(4, results.size)

        results[0].apply {
            assertEquals(null, this.ignored)
            assertEquals(1, this.col11)
            assertEquals(2, this.col12)
            assertEquals("entity 1", this.col13)

            // left joined to one
            assertNotNull(this.entity2)
            assertEquals(1, this.entity2!!.col21)
            assertEquals(2, this.entity2!!.col22)
            assertEquals("entity 1", this.entity2!!.col23)
            assertEquals(1, this.entity2!!.entity3List!!.size)

            // left joined to many
            assertEquals(1, this.entity2!!.entity3List!![0].col31)
            assertEquals(2, this.entity2!!.entity3List!![0].col32)
            assertEquals("entity 1", this.entity2!!.entity3List!![0].col33)
        }

        results[1].apply {
            assertEquals(2, this.col11)
            assertEquals(3, this.col12)
            assertEquals("entity 2", this.col13)

            // left joined to one
            assertNotNull(this.entity2)
            assertEquals(3, this.entity2!!.col21)
            assertEquals(4, this.entity2!!.col22)
            assertEquals("entity 3", this.entity2!!.col23)
            assertEquals(2, this.entity2!!.entity3List!!.size)

            // left joined to many
            assertEquals(2, this.entity2!!.entity3List!![0].col31)
            assertEquals(3, this.entity2!!.entity3List!![0].col32)
            assertEquals("entity 2", this.entity2!!.entity3List!![0].col33)

            assertEquals(4, this.entity2!!.entity3List!![1].col31)
            assertEquals(5, this.entity2!!.entity3List!![1].col32)
            assertEquals("entity 3", this.entity2!!.entity3List!![1].col33)
        }

        results[2].apply {
            assertEquals(3, this.col11)
            assertEquals(4, this.col12)
            assertEquals("entity 3", this.col13)

            // left joined to one
            assertNotNull(this.entity2)
            assertEquals(2, this.entity2!!.col21)
            assertEquals(3, this.entity2!!.col22)
            assertEquals("entity 2", this.entity2!!.col23)
            assertEquals(0, this.entity2!!.entity3List!!.size)
        }

        results[3].apply {
            assertEquals(4, this.col11)
            assertEquals(5, this.col12)
            assertEquals("entity 4", this.col13)

            // left joined to one
            assertNull(this.entity2)
        }
    }
}