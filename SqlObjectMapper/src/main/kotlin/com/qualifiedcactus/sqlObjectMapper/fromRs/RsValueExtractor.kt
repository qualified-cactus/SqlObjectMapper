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
import java.util.*
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf



/**
 * Extract value from [ResultSet] to put into property.
 * Each property has an instance of this class.
 * All implementation must satisfy the following conditions:
 *
 * - is thread safe
 * - have a constructor([KType], [KAnnotatedElement]) to be used by this library
 *
 * @see DefaultRsValueExtractor
 *
 */
abstract class RsValueExtractor(
    protected val propertyType: KType,
    protected val propertyAnnotations: KAnnotatedElement,
) {
    companion object {
        internal fun createInstance(
            kClass: KClass<out RsValueExtractor>,
            kType: KType,
            kAnnotatedElement: KAnnotatedElement
        ): RsValueExtractor {
            return kClass.constructors.find {
                it.parameters.size == 2 &&
                    it.parameters[0].type.classifier == KType::class &&
                    it.parameters[1].type.classifier == KAnnotatedElement::class
            }?.call(kType, kAnnotatedElement)
                ?: throw SqlObjectMapperException(
                    "${kClass} doesn't have constructor of type (KType, KAnnotatedElement)"
                )
        }
    }
    protected val propertyClass = propertyType.classifier as KClass<*>

    protected open fun provideValueFromName(rs: ResultSet, columnName: String): Any? {
        return rs.getObject(columnName)
    }

    protected open fun provideValueFromIndex(rs: ResultSet, columnIndex: Int): Any? {
        return rs.getObject(columnIndex)
    }

    protected open fun processValue(rs: ResultSet, value: Any?): Any? {
        return value
    }

    fun extractValueByName(rs: ResultSet, columnName: String): Any? =
        processValue(rs, provideValueFromName(rs, columnName))

    fun extractValueByIndex(rs: ResultSet, columnIndex: Int): Any? =
        processValue(rs, provideValueFromIndex(rs, columnIndex))
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
 * - [OffsetDateTime] (with UTC)
 * - [OffsetTime] (with UTC)
 * - [Instant]
 * - [ZonedDateTime] (with UTC)
 * - Subclasses of [Enum]
 *
 *  If property's type doesn't match above types, [ResultSet.getObject] is used.
 *
 *  Note that JDBC's [java.sql.Time] and [java.sql.Timestamp] doesn't support date time with offset,
 *  so don't think that your offset information is saved into the database.
 */
class DefaultRsValueExtractor(
    propertyType: KType,
    propertyAnnotations: KAnnotatedElement,
) : RsValueExtractor(propertyType, propertyAnnotations) {

    /**
     * Use to check if sql primitive is null
     */
    private fun Any.checkPrimitiveNull(rs: ResultSet): Any? {
        return if (rs.wasNull()) null else this
    }

    override fun provideValueFromName(rs: ResultSet, columnName: String): Any? {
        return when (propertyClass) {

            Long::class -> rs.getLong(columnName).checkPrimitiveNull(rs)
            Int::class -> rs.getInt(columnName).checkPrimitiveNull(rs)
            Double::class -> rs.getDouble(columnName).checkPrimitiveNull(rs)
            Short::class -> rs.getShort(columnName).checkPrimitiveNull(rs)
            Boolean::class -> rs.getBoolean(columnName).checkPrimitiveNull(rs)
            Byte::class -> rs.getByte(columnName).checkPrimitiveNull(rs)

            BigDecimal::class -> rs.getBigDecimal(columnName)
            ByteArray::class -> rs.getBytes(columnName)
            String::class -> rs.getString(columnName)

            LocalTime::class -> rs.getTime(columnName)?.toLocalTime()
            LocalDate::class -> rs.getDate(columnName)?.toLocalDate()
            LocalDateTime::class -> rs.getTimestamp(columnName)?.toLocalDateTime()

            OffsetDateTime::class -> rs.getTimestamp(columnName)?.toLocalDateTime()?.let { OffsetDateTime.of(it, ZoneOffset.UTC)}
            OffsetTime::class -> rs.getTime(columnName)?.toLocalTime()?.let { OffsetTime.of(it, ZoneOffset.UTC) }
            Instant::class -> rs.getTimestamp(columnName)?.toInstant()
            ZonedDateTime::class -> rs.getTimestamp(columnName)?.toLocalDateTime()?.let { ZonedDateTime.of(it, ZoneOffset.UTC) }

            else -> {
                if (propertyClass.isSubclassOf(Enum::class)) {
                    rs.getString(columnName)?.toEnum(propertyClass)
                } else {
                    rs.getObject(columnName)
                }
            }
        }
    }

    override fun provideValueFromIndex(rs: ResultSet, columnIndex: Int): Any? {
        return when (propertyClass) {

            Long::class -> rs.getLong(columnIndex).checkPrimitiveNull(rs)
            Int::class -> rs.getInt(columnIndex).checkPrimitiveNull(rs)
            Double::class -> rs.getDouble(columnIndex).checkPrimitiveNull(rs)
            Short::class -> rs.getShort(columnIndex).checkPrimitiveNull(rs)
            Boolean::class -> rs.getBoolean(columnIndex).checkPrimitiveNull(rs)
            Byte::class -> rs.getByte(columnIndex).checkPrimitiveNull(rs)

            BigDecimal::class -> rs.getBigDecimal(columnIndex)
            ByteArray::class -> rs.getBytes(columnIndex)
            String::class -> rs.getString(columnIndex)

            LocalTime::class -> rs.getTime(columnIndex)?.toLocalTime()
            LocalDate::class -> rs.getDate(columnIndex)?.toLocalDate()
            LocalDateTime::class -> rs.getTimestamp(columnIndex)?.toLocalDateTime()

            OffsetDateTime::class -> rs.getTimestamp(columnIndex)?.toLocalDateTime()?.let { OffsetDateTime.of(it, ZoneOffset.UTC)}
            OffsetTime::class -> rs.getTime(columnIndex)?.toLocalTime()?.let { OffsetTime.of(it, ZoneOffset.UTC) }
            Instant::class -> rs.getTimestamp(columnIndex)?.toInstant()
            ZonedDateTime::class -> rs.getTimestamp(columnIndex)?.toLocalDateTime()?.let { ZonedDateTime.of(it, ZoneOffset.UTC) }

            else -> {
                if (propertyClass.isSubclassOf(Enum::class)) {
                    rs.getString(columnIndex)?.toEnum(propertyClass)
                } else {
                    rs.getObject(columnIndex)
                }
            }
        }
    }

    private fun String.toEnum(propertyClass: KClass<*>): Enum<*> =
        java.lang.Enum.valueOf(propertyClass.java as Class<out Enum<*>>, this)
}

class RsByteArrayToUuidExtractor(
    propertyType: KType,
    propertyAnnotations: KAnnotatedElement,
) : RsValueExtractor(propertyType, propertyAnnotations) {
    override fun provideValueFromName(rs: ResultSet, columnName: String): ByteArray? {
        return rs.getBytes(columnName)
    }

    override fun provideValueFromIndex(rs: ResultSet, columnIndex: Int): ByteArray? {
        return rs.getBytes(columnIndex)
    }

    override fun processValue(rs: ResultSet, value: Any?): UUID? {
        if (value == null) {
            return null
        }
        if (value !is ByteArray) throw RuntimeException()

        if (value.size != 16) {
            throw SqlObjectMapperException(
                "Invalid converter usage: byte array size (${value.size}) is not 16"
            )
        }
        val byteBuffer = ByteBuffer.wrap(value)
        return UUID(
            byteBuffer.long,
            byteBuffer.long
        )
    }
}

