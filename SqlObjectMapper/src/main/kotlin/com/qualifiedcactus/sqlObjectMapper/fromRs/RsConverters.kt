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
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


//
///**
// * Convert string to enum
// */
//class RsStringToEnumConverter : RsValueConverter {
//    override fun convert(value: Any?, propertyType: KClass<*>): Any? {
//        if (value == null) return null
//
//        val stringValue = value as? String
//            ?: throw SqlObjectMapperException("Tried to convert a non-string type ${value::class} to enum")
//
//        if (propertyType.isSubclassOf(Enum::class)) {
//            return java.lang.Enum.valueOf(propertyType.java as Class<out Enum<*>>, stringValue)
//        }
//        else throw SqlObjectMapperException("Tried to use enum on non enum type")
//    }
//}
//
//
///**
// * Convert database RAW data type (bytes array) to [UUID]
// */
//class RsByteArrayToUuidConverter : RsValueConverter {
//
//    /**
//     * Convert byte array to uuid
//     */
//    override fun convert(value: Any?, propertyType: KClass<*>): UUID? {
//        //reference: https://www.baeldung.com/java-byte-array-to-uuid
//
//        if (value == null) return null
//        if (value is ByteArray) {
//            if (value.size != 16) {
//                throw SqlObjectMapperException("Invalid converter usage: byte array size (${value.size}) is not 16")
//            }
//            val byteBuffer = ByteBuffer.wrap(value)
//            return UUID(
//                byteBuffer.long,
//                byteBuffer.long
//            )
//        }
//        else {
//            throw SqlObjectMapperException(
//                "Invalid converter usage: value (${value::class}) to be converted is not a byte array"
//            )
//        }
//    }
//}