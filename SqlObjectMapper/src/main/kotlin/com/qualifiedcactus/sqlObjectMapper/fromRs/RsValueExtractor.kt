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
@file:Suppress("UNCHECKED_CAST")
package com.qualifiedcactus.sqlObjectMapper.fromRs

import com.qualifiedcactus.sqlObjectMapper.SqlObjectMapperException
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.sql.ResultSet
import java.time.*
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

/**
 * Extract value from [ResultSet] to put into property.
 * All implementation must be thread safe and must have a no-arg constructor.
 */
interface RsValueExtractor {

    fun extractValueByName(rs: ResultSet, propertyType: KType, columnName: String): Any?

    fun extractValueByIndex(rs: ResultSet, propertyType: KType, columnIndex: Int): Any?
}

/**
 * Extract value from [ResultSet]. Appropriate [ResultSet]'s method are called for the following types:
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
 * - [LocalDate]
 * - [LocalTime]
 * - [LocalDateTime]
 * - [OffsetDateTime] (use system default zone id)
 * - [OffsetTime] (use utc zone offset)
 * - [Instant]
 * - [ZonedDateTime] (use system default zone id)
 * - Subclasses of [Enum]
 *
 *  If property's type doesn't match above types, [ResultSet.getObject] is used.
 *
 *  Note that JDBC's [java.sql.Time] and [java.sql.Timestamp] doesn't support date time with offset,
 *  so don't think that your offset information is saved into the database.
 */
class DefaultRsValueExtractor : RsValueExtractor {

    override fun extractValueByName(rs: ResultSet, propertyType: KType, columnName: String): Any? {

        val value: Any? = when (val propertyClass = propertyType.classifier as KClass<*>) {

            Long::class -> rs.getLong(columnName)
            Int::class -> rs.getInt(columnName)
            BigDecimal::class -> rs.getBigDecimal(columnName)
            Double::class -> rs.getDouble(columnName)
            Short::class -> rs.getDouble(columnName)
            Boolean::class -> rs.getBoolean(columnName)
            String::class -> rs.getString(columnName)
            Byte::class -> rs.getByte(columnName)
            ByteArray::class -> rs.getBytes(columnName)

            LocalTime::class -> rs.getTime(columnName)?.toLocalTime()
            LocalDate::class -> rs.getDate(columnName)?.toLocalDate()
            LocalDateTime::class -> rs.getTimestamp(columnName)?.toLocalDateTime()

            OffsetDateTime::class -> rs.getTimestamp(columnName)?.toInstant()
                ?.let{OffsetDateTime.ofInstant(it, ZoneId.systemDefault())}
            OffsetTime::class ->  rs.getTime(columnName)?.toLocalTime()?.let { OffsetTime.of(it, ZoneOffset.UTC)}
            Instant::class -> rs.getTimestamp(columnName)?.toInstant()
            ZonedDateTime::class -> rs.getTimestamp(columnName)?.toInstant()
                ?.let{ZonedDateTime.ofInstant(it, ZoneId.systemDefault())}


            else -> {
                if (propertyClass.isSubclassOf(Enum::class)) {
                    rs.getString(columnName)?.toEnum(propertyClass)
                }
                else {
                    rs.getObject(columnName)
                }
            }
        }
        return value
    }

    override fun extractValueByIndex(rs: ResultSet, propertyType: KType, columnIndex: Int): Any? {
        val value: Any? = when (val propertyClass = propertyType.classifier as KClass<*>) {

            Long::class -> rs.getLong(columnIndex)
            Int::class -> rs.getInt(columnIndex)
            BigDecimal::class -> rs.getBigDecimal(columnIndex)
            Double::class -> rs.getDouble(columnIndex)
            Short::class -> rs.getDouble(columnIndex)
            Boolean::class -> rs.getBoolean(columnIndex)
            String::class -> rs.getString(columnIndex)
            Byte::class -> rs.getByte(columnIndex)
            ByteArray::class -> rs.getBytes(columnIndex)

            LocalTime::class -> rs.getTime(columnIndex)?.toLocalTime()
            LocalDate::class -> rs.getDate(columnIndex)?.toLocalDate()
            LocalDateTime::class -> rs.getTimestamp(columnIndex)?.toLocalDateTime()

            OffsetDateTime::class -> rs.getTimestamp(columnIndex)?.toInstant()
                ?.let{OffsetDateTime.ofInstant(it, ZoneId.systemDefault())}
            OffsetTime::class ->  rs.getTime(columnIndex)?.toLocalTime()?.let { OffsetTime.of(it, ZoneOffset.UTC)}
            Instant::class -> rs.getTimestamp(columnIndex)?.toInstant()
            ZonedDateTime::class -> rs.getTimestamp(columnIndex)?.toInstant()
                ?.let{ZonedDateTime.ofInstant(it, ZoneId.systemDefault())}

            else -> {
                if (propertyClass.isSubclassOf(Enum::class)) {
                    rs.getString(columnIndex)?.toEnum(propertyClass)
                }
                else {
                    rs.getObject(columnIndex)
                }
            }
        }
        return value
    }

    private fun String.toEnum(propertyClass: KClass<*>): Enum<*>
        = java.lang.Enum.valueOf(propertyClass.java as Class<out Enum<*>>, this)

}

class RsByteArrayToUuidExtractor : RsValueExtractor {

    override fun extractValueByName(rs: ResultSet, propertyType: KType, columnName: String): Any? {
        return rs.getBytes(columnName)?.toUuid()
    }

    override fun extractValueByIndex(rs: ResultSet, propertyType: KType, columnIndex: Int): Any? {
        return rs.getBytes(columnIndex)?.toUuid()
    }

    private fun ByteArray.toUuid(): UUID {
        if (this.size != 16) {
            throw SqlObjectMapperException("Invalid converter usage: byte array size (${this.size}) is not 16")
        }
        val byteBuffer = ByteBuffer.wrap(this)
        return UUID(
            byteBuffer.long,
            byteBuffer.long
        )
    }
}