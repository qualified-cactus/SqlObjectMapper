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

import com.qualifiedcactus.sqlObjectMapper.SqlObjectMapperException
import java.sql.ResultSet


internal class IdMapping(
    val idColumnNames: List<String>
) {
    val noId = idColumnNames.isEmpty()

    /**
     * @return null if all id columns is null
     */
    fun fromResultSet(resultSet: ResultSet): IdValue? {
        if (noId) {
            throw IllegalStateException("Internal Error: invalid operation")
        }
        return if (idColumnNames.size == 1) {
            toSimpleId(resultSet)
        } else if (idColumnNames.size > 1) {
            toCompositeId(resultSet)
        } else {
            throw SqlObjectMapperException("No ID columns specified")
        }
    }

    private fun toSimpleId(resultSet: ResultSet): SimpleIdValue? {
        val idValue = resultSet.getObject(idColumnNames.first())
        return if (idValue == null)
            null
        else
            SimpleIdValue(idValue)
    }

    private fun toCompositeId(resultSet: ResultSet): CompositeIdValue? {
        val idValues = arrayOfNulls<Any?>(idColumnNames.size)
        var allNull = false
        for (i in idColumnNames.indices) {
            resultSet.getObject(idColumnNames[i]).also { id: Any? ->
                idValues[i] = id
                allNull = allNull || (id == null)
            }
        }
        return if (allNull) null else CompositeIdValue(idValues)
    }

    interface IdValue

    class SimpleIdValue(private val idValue: Any) : IdValue {
        override fun hashCode(): Int {
            return idValue.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return other is SimpleIdValue && other.idValue == idValue
        }
    }

    class CompositeIdValue(
        private val idValues: Array<Any?>
    ) : IdValue {
        private val hashCode: Int

        init {
            var _hashCode = 1
            val prime = 59
            for (idValue in idValues) {
                if (idValue != null) {
                    _hashCode = _hashCode * prime + idValue.hashCode()
                } else {
                    _hashCode = _hashCode * prime
                }
            }
            hashCode = _hashCode
        }

        override fun hashCode(): Int {
            return hashCode
        }

        override fun equals(other: Any?): Boolean {
            return other is CompositeIdValue && other.idValues.contentEquals(idValues)
        }

    }
}


