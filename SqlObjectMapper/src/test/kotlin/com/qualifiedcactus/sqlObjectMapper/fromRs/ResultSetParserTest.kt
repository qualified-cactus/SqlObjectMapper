package com.qualifiedcactus.sqlObjectMapper.fromRs

import com.qualifiedcactus.sqlObjectMapper.createDatabaseConnection
import com.qualifiedcactus.sqlObjectMapper.fromRs.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import java.sql.Connection


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ResultSetParserTest : AutoCloseable {

    val tableSql = """
        
        CREATE TABLE table_a (
            column_1 INTEGER UNIQUE NOT NULL,
            column_2 INTEGER UNIQUE NOT NULL,
            column_3 INTEGER NOT NULL,
            PRIMARY KEY (column_1, column_2)
        );
        
        CREATE TABLE table_b (
            column_4 INTEGER PRIMARY KEY,
            column_5 INTEGER NOT NULL REFERENCES table_a(column_1)
        );
        
        CREATE TABLE table_c (
            column_6 INTEGER PRIMARY KEY,
            column_7 INTEGER NOT NULL REFERENCES table_b(column_4)
        );
        
    """

    val dataSql = """
        INSERT INTO table_a VALUES (1,2,3);
        INSERT INTO table_a VALUES (4,5,6);
        
        INSERT INTO table_b VALUES (1,1);
        INSERT INTO table_b VALUES (2,1);
        
        INSERT INTO table_c VALUES (1,1);
        INSERT INTO table_c VALUES (2,1);
    """.trimIndent()

    data class EntityA(
        @RsColumn(isId = true)
        val column1: Int,
        @RsColumn(isId = true)
        val column2: Int,
        val column3: Int,
        @RsToMany
        val listOfB: List<EntityB>
    )

    data class EntityB(
        @RsColumn(isId = true)
        val column4: Int,
        @RsToMany
        val listOfC: List<EntityC>,
    )

    data class EntityC(
        @RsColumn(isId = true)
        val column6: Int
    )

    data class EntityB2(
        @RsColumn(isId = true)
        val column4: Int,
        @RsToMany(classToMap = EntityC::class, elementConverter = EntityCToInt::class)
        val intList: List<Int>
    )

    class EntityCToInt : RsValueConverter {
        override fun convert(value: Any?): Int {
            return (value as EntityC).column6
        }
    }

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

    @Test
    fun parseToScalarTest() {
        val query = "SELECT count(*) FROM table_b"

        val count = connection.createStatement().use { stmt ->
            stmt.executeQuery(query).toScalar<Long>()
        }
        assertEquals(2, count)
    }

    data class SimpleA(
        val column1: Int,
        val column2: Int,
        val column3: Int
    )

    @Test
    fun parseToObjectTest() {
        val query = "SELECT * FROM table_a WHERE column_1 = 1"
        val entity = connection.createStatement().use { stmt ->
            stmt.executeQuery(query).toObject(SimpleA::class)
        }
        assertNotNull(entity)
        assertEquals(1, entity!!.column1)
        assertEquals(2, entity.column2)
        assertEquals(3, entity.column3)

    }

    @Test
    fun parseToListTest() {
        val query = """
            SELECT *
            FROM table_a a
            LEFT JOIN table_b b ON a.column_1 = b.column_5
            LEFT JOIN table_c c ON b.column_4 = c.column_7
        """.trimIndent()

        val listOfA: List<EntityA> = connection.createStatement().use { stmt ->
            stmt.executeQuery(query).use { rs ->
                rs.toList(EntityA::class)
            }
        }
        assertEquals(2, listOfA.size)

        val a12 = listOfA.find { it.column1 == 1 }
        assertNotNull(a12)
        assertEquals(2, a12!!.listOfB.size)

        val b1 = requireNotNull(a12.listOfB.find { it.column4 == 1 })
        val b2 = requireNotNull(a12.listOfB.find { it.column4 == 2 })

        assertEquals(2, b1.listOfC.size)
        assertEquals(0, b2.listOfC.size)

        assertNotNull(b1.listOfC.find { it.column6 == 1 })
        assertNotNull(b1.listOfC.find { it.column6 == 2 })


        val a45 = listOfA.find { it.column1 == 4 }
        assertNotNull(a45)
        assertEquals(0, a45!!.listOfB.size)
    }

    @Test
    fun parseToListTest2() {
        val query = """
            SELECT *
            FROM table_b b
            LEFT JOIN table_c c ON b.column_4 = c.column_7
        """.trimIndent()

        val bList = connection.createStatement()
            .use { it.executeQuery(query).toList(EntityB2::class) }
        assertEquals(2, bList.size)
        val b1 = bList.find { it.column4 == 1 }
        assertNotNull(b1)
        assertEquals(2, b1!!.intList.size)
        assertTrue(b1.intList.contains(1))
        assertTrue(b1.intList.contains(2))

        assertNotNull(bList.find { it.column4 == 2 })

    }
}