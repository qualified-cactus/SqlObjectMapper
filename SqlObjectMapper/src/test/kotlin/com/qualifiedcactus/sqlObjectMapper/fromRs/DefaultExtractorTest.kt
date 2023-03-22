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
import com.qualifiedcactus.sqlObjectMapper.prepareNpStatement
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.time.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DefaultExtractorTest : AutoCloseable {
    companion object {

        private const val table = """
CREATE TABLE value_extractor_test(
    id INTEGER PRIMARY KEY,

    long_column BIGINT,
    int_column INTEGER,
    double_column NUMERIC(38,16),
    short_column INTEGER,
    boolean_column TINYINT,
    byte_column TINYINT,

    big_decimal_column NUMERIC(38,16),
    byte_array_column BLOB,
    string_column VARCHAR(50),

    time_column TIME,
    date_column DATE,
    date_time_column TIMESTAMP,

    offset_date_time_column TIMESTAMP,
    offset_time_column TIME,
    instant_column TIMESTAMP,
    zoned_datetime_column TIMESTAMP,

    enum_column VARCHAR(50)
);
        """

        private const val insertStatement = """
INSERT INTO value_extractor_test VALUES (
    :id,

    :long_column,
    :int_column,
    :double_column,
    :short_column,
    :boolean_column,
    :byte_column,

    :big_decimal_column,
    :byte_array_column,
    :string_column,

    :time_column,
    :date_column,
    :date_time_column,

    :offset_date_time_column,
    :offset_time_column,
    :instant_column,
    :zoned_datetime_column,

    :enum_column
)
"""
    }

    val connection = createDatabaseConnection()

    init {
        connection.createStatement().use { stmt ->
            stmt.execute(table)
        }
    }

    @Test
    fun testDefaultExtractor() {
        val dto1 = Dto1(
            id = 1,

            longColumn = 10000,
            intColumn = 10000,
            doubleColumn = 12345.12345,
            shortColumn = 123,
            booleanColumn = true,
            byteColumn = 10,

            bigDecimalColumn = BigDecimal("12345.12345"),
            byteArrayColumn = byteArrayOf(12, 23, 45),
            stringColumn = "this is a string",

            timeColumn = LocalTime.of(5, 14, 30),
            dateColumn = LocalDate.of(2020, 5, 12),
            dateTimeColumn = LocalDateTime.of(
                LocalDate.of(2020, 5, 12),
                LocalTime.of(5, 14, 30)
            ),

            offsetDateTimeColumn = OffsetDateTime.of(
                LocalDate.of(2020, 5, 12),
                LocalTime.of(5, 14, 30),
                ZoneOffset.UTC
            ),
            offsetTimeColumn = OffsetTime.of(
                LocalTime.of(5, 14, 30),
                ZoneOffset.UTC
            ),
            instantColumn = Instant.ofEpochMilli(12345678L),
            zonedDatetimeColumn = ZonedDateTime.of(
                LocalDate.of(2020, 5, 12),
                LocalTime.of(5, 14, 30),
                ZoneOffset.UTC
            ),

            enumColumn = DtoEnum.SecondEnum
        )

        connection.prepareNpStatement(insertStatement)
            .setParametersByDto(dto1)
            .useExecuteUpdate()

        val storedDto = connection.prepareNpStatement(
            """
            SELECT * FROM value_extractor_test WHERE id = :id
        """
        ).setParameter("id", 1).useExecuteQuery { it.toObject(Dto1::class) }!!

        assertEquals(dto1.id, storedDto.id)

        assertEquals(dto1.longColumn, storedDto.longColumn)
        assertEquals(dto1.intColumn, storedDto.intColumn)
        assertEquals(dto1.doubleColumn, storedDto.doubleColumn)
        assertEquals(dto1.shortColumn, storedDto.shortColumn)
        assertEquals(dto1.booleanColumn, storedDto.booleanColumn)
        assertEquals(dto1.byteColumn, storedDto.byteColumn)

        assertEquals(0, dto1.bigDecimalColumn!!.compareTo(storedDto.bigDecimalColumn))
        assertTrue(dto1.byteArrayColumn.contentEquals(storedDto.byteArrayColumn))
        assertEquals(dto1.stringColumn, storedDto.stringColumn)

        assertEquals(dto1.timeColumn, storedDto.timeColumn)
        assertEquals(dto1.dateColumn, storedDto.dateColumn)
        assertEquals(dto1.dateTimeColumn, storedDto.dateTimeColumn)

        assertEquals(dto1.offsetDateTimeColumn!!.toInstant(), storedDto.offsetDateTimeColumn!!.toInstant())
        assertEquals(
            dto1.offsetTimeColumn!!.toEpochSecond(LocalDate.EPOCH),
            storedDto.offsetTimeColumn!!.toEpochSecond(LocalDate.EPOCH)
        )
        assertEquals(dto1.instantColumn, storedDto.instantColumn)
        assertEquals(dto1.zonedDatetimeColumn!!.toInstant(), storedDto.zonedDatetimeColumn!!.toInstant())

        assertEquals(dto1.enumColumn, storedDto.enumColumn)
    }

    @Test
    fun testDefaultExtractorWithNull() {
        val dto1 = Dto1(
            id = 2,

            longColumn = null,
            intColumn = null,
            doubleColumn = null,
            shortColumn = null,
            booleanColumn = null,
            byteColumn = null,

            bigDecimalColumn = null,
            byteArrayColumn = null,
            stringColumn = null,

            timeColumn = null,
            dateColumn = null,
            dateTimeColumn = null,

            offsetDateTimeColumn = null,
            offsetTimeColumn = null,
            instantColumn = null,
            zonedDatetimeColumn = null,

            enumColumn = null
        )

        connection.prepareNpStatement(insertStatement)
            .setParametersByDto(dto1)
            .useExecuteUpdate()

        val storedDto = connection.prepareNpStatement(
            """
            SELECT * FROM value_extractor_test WHERE id = :id
        """
        ).setParameter("id", 2).useExecuteQuery { it.toObject(Dto1::class) }!!

        assertEquals(dto1.id, storedDto.id)

        assertNull(storedDto.longColumn)
        assertNull(storedDto.intColumn)
        assertNull(storedDto.doubleColumn)
        assertNull(storedDto.shortColumn)
        assertNull(storedDto.booleanColumn)
        assertNull(storedDto.byteColumn)

        assertNull(storedDto.bigDecimalColumn)
        assertNull(storedDto.byteArrayColumn)
        assertNull(storedDto.stringColumn)

        assertNull(storedDto.timeColumn)
        assertNull(storedDto.dateColumn)
        assertNull(storedDto.dateTimeColumn)

        assertNull(storedDto.offsetDateTimeColumn)
        assertNull(storedDto.offsetTimeColumn)
        assertNull(storedDto.instantColumn)
        assertNull(storedDto.zonedDatetimeColumn)

        assertNull(storedDto.enumColumn)
    }

    override fun close() {
        connection.close()
    }

    class Dto1(
        val id: Int,

        val longColumn: Long?,
        val intColumn: Int?,
        val doubleColumn: Double?,
        val shortColumn: Short?,
        val booleanColumn: Boolean?,
        val byteColumn: Byte?,

        val bigDecimalColumn: BigDecimal?,
        val byteArrayColumn: ByteArray?,
        val stringColumn: String?,

        val timeColumn: LocalTime?,
        val dateColumn: LocalDate?,
        val dateTimeColumn: LocalDateTime?,

        val offsetDateTimeColumn: OffsetDateTime?,
        val offsetTimeColumn: OffsetTime?,
        val instantColumn: Instant?,
        val zonedDatetimeColumn: ZonedDateTime?,

        val enumColumn: DtoEnum?,
    )

    enum class DtoEnum {
        FirstEnum, SecondEnum
    }
}