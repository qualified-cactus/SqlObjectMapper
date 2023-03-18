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

package com.qualifiedcactus.sqlObjectMapper.toParam

import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.*

/**
 * All implementations must be thread safe.
 *
 */
interface ParamValueSetter {
    fun setParam(stmt: PreparedStatement, paramIndex: Int, value: Any?, objectCreator: JdbcObjectCreator)
}

/**
 * Use explicit [PreparedStatement]'s parameter setter methods for the following types:
 *
 * - [Long]
 * - [Int]
 * - [BigDecimal]
 * - [Double]
 * - [Short]
 * - [Boolean]
 * - [String]
 * - [Byte]
 * - [ByteArray]
 * - [LocalTime] -> [java.sql.Time] (precision beyond millisecond is lost)
 * - [LocalDate] -> [java.sql.Date]
 * - [LocalDateTime] -> [java.sql.Timestamp]
 * - [OffsetDateTime] -> [java.sql.Timestamp] (offset is converted to UTC)
 * - [OffsetTime] -> [java.sql.Time] (converted to UTC before setting) (precision beyond millisecond is lost)
 * - [Instant] -> [java.sql.Timestamp]
 * - [ZonedDateTime] -> [java.sql.Timestamp] (Zone converted to UTC)
 * - [Enum] -> [String]
 *
 * [PreparedStatement.setObject] is used for values that don't match above types.
 *
 * Note that JDBC's [java.sql.Time] and [java.sql.Timestamp] doesn't support date time with offset,
 * so don't think that your offset information is saved into the database.
 *
 */
class DefaultParamValueSetter : ParamValueSetter {
    override fun setParam(stmt: PreparedStatement, paramIndex: Int, value: Any?, objectCreator: JdbcObjectCreator) {
        when (value) {
            null -> stmt.setObject(paramIndex, null)
            is Long -> stmt.setLong(paramIndex, value)
            is Int -> stmt.setInt(paramIndex, value)
            is BigDecimal -> stmt.setBigDecimal(paramIndex, value)
            is Double -> stmt.setDouble(paramIndex, value)
            is Short -> stmt.setShort(paramIndex, value)
            is Boolean -> stmt.setBoolean(paramIndex, value)
            is String -> stmt.setString(paramIndex, value)
            is Byte -> stmt.setByte(paramIndex, value)
            is ByteArray -> stmt.setBytes(paramIndex, value)

            is LocalTime -> stmt.setTime(paramIndex, java.sql.Time(value.toNanoOfDay() / 1_000_000L))
            is LocalDate -> stmt.setDate(
                paramIndex,
                java.sql.Date(value.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC) * 1_000)
            )
            is LocalDateTime -> stmt.setTimestamp(
                paramIndex,
                value.toInstant(ZoneOffset.UTC)
                    .run { java.sql.Timestamp(toEpochMilli()).apply { nanos = this@run.nano } }
            )
            is OffsetDateTime -> stmt.setTimestamp(paramIndex,
                java.sql.Timestamp(value.toInstant().toEpochMilli())
                    .apply { nanos = value.nano }
            )
            is OffsetTime -> stmt.setTime(
                paramIndex, java.sql.Time(
                    value.withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond(LocalDate.EPOCH) * 1_000
                )
            )
            is Instant -> stmt.setTimestamp(paramIndex, java.sql.Timestamp(value.toEpochMilli()))
            is ZonedDateTime -> stmt.setTimestamp(paramIndex,
                java.sql.Timestamp(value.toInstant().toEpochMilli()).apply { nanos = value.nano }
            )

            is Enum<*> -> stmt.setString(paramIndex, value.name)
            else -> stmt.setObject(paramIndex, value)

        }
    }
}