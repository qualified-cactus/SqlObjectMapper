package com.qualifiedcactus.sqlObjectMapper

import com.qualifiedcactus.sqlObjectMapper.fromRs.toList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.sql.Connection

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class NpStatementTest {

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


}