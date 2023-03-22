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

package com.qualifiedcactus.sqlObjectMapper.toParam

import com.qualifiedcactus.sqlObjectMapper.SqlObjectMapperException
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.sql.PreparedStatement
import java.time.*
import java.util.UUID
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType


/**
 * Pass from property to [PreparedStatement].
 * Each property has an instance of this class.
 * All implementation must satisfy the following conditions:
 *
 * - is thread safe
 * - have a constructor([KType], [KAnnotatedElement]) to be used by this library
 *
 * @see DefaultParamValueSetter
 *
 */
abstract class ParamValueSetter(
    protected val propertyType: KType,
    protected val propertyAnnotations: KAnnotatedElement,
) {
    companion object {
        internal fun createInstance(
            kClass: KClass<out ParamValueSetter>,
            kType: KType,
            kAnnotatedElement: KAnnotatedElement,
        ): ParamValueSetter {
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


    protected open fun processValue(
        propertyValue: Any?,
        objectCreator: JdbcObjectCreator
    ): Any? {
        return propertyValue
    }

    protected open fun setValueIntoStatement(
        stmt: PreparedStatement, paramIndex: Int, value: Any?
    ) {
        stmt.setObject(paramIndex, value)
    }

    fun setParam(
        stmt: PreparedStatement, paramIndex: Int,
        propertyValue: Any?, objectCreator: JdbcObjectCreator
    ) {
        setValueIntoStatement(stmt, paramIndex, processValue(propertyValue, objectCreator))
    }
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
 * - [LocalTime] -> [java.sql.Time]
 * - [LocalDate] -> [java.sql.Date]
 * - [LocalDateTime] -> [java.sql.Timestamp]
 * - [OffsetDateTime] -> [java.sql.Timestamp] (converted to UTC)
 * - [OffsetTime] -> [java.sql.Time] (converted to UTC)
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
class DefaultParamValueSetter(
    propertyType: KType,
    propertyAnnotations: KAnnotatedElement,
) : ParamValueSetter(propertyType, propertyAnnotations) {

    companion object {
        internal fun setIntoStatement(stmt: PreparedStatement, paramIndex: Int, value: Any?) {
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

                is LocalTime -> stmt.setTime(paramIndex, java.sql.Time.valueOf(value))
                is LocalDate -> stmt.setDate(paramIndex, java.sql.Date.valueOf(value))
                is LocalDateTime -> stmt.setTimestamp(paramIndex, java.sql.Timestamp.valueOf(value))
                is OffsetDateTime -> stmt.setTimestamp(paramIndex,
                    java.sql.Timestamp.valueOf(value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime())
                )
                is OffsetTime -> stmt.setTime(paramIndex,
                    java.sql.Time.valueOf(value.withOffsetSameInstant(ZoneOffset.UTC).toLocalTime())
                )
                is Instant -> stmt.setTimestamp(paramIndex, java.sql.Timestamp(value.toEpochMilli()))
                is ZonedDateTime -> stmt.setTimestamp(paramIndex,
                    java.sql.Timestamp.valueOf(value.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime())
                )

                is Enum<*> -> stmt.setString(paramIndex, value.name)
                else -> stmt.setObject(paramIndex, value)
            }
        }
    }
    override fun setValueIntoStatement(stmt: PreparedStatement, paramIndex: Int, value: Any?) {
        setIntoStatement(stmt, paramIndex, value)
    }
}


/**
 * Convert UUID to byte array
 */
class ParamUuidToByteArraySetter(
    propertyType: KType,
    propertyAnnotations: KAnnotatedElement,
) : ParamValueSetter(propertyType, propertyAnnotations) {

    override fun processValue(propertyValue: Any?, objectCreator: JdbcObjectCreator): ByteArray? {
        if (propertyValue == null) return null
        if (propertyValue !is UUID) throw SqlObjectMapperException("Property is an UUID")

        val byteBuffer = ByteBuffer.wrap(ByteArray(16))
        byteBuffer.putLong(propertyValue.mostSignificantBits)
        byteBuffer.putLong(propertyValue.leastSignificantBits)
        return byteBuffer.array()
    }

    override fun setValueIntoStatement(stmt: PreparedStatement, paramIndex: Int, value: Any?) {
        stmt.setBytes(paramIndex, value as ByteArray?)
    }
}