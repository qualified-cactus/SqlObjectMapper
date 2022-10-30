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

package sqlObjectMapper.annotationProcessing

import sqlObjectMapper.SqlObjectMapperException

/**
 * This class is used to represent how to get id value(s) of an entity.
 */
class IdMapping(val idColumnNames: Collection<String>) {
    val combinedNames: String
    init {
        if (idColumnNames.isEmpty()) {
            combinedNames = ""
        }
        else {
            val s = StringBuilder()
            for (colName in idColumnNames) {
                s.append(colName).append(';')
            }
            combinedNames = s.toString()
        }

    }

    fun getValue(valueProvider: ValueProvider): IdValue? {
        if (idColumnNames.isEmpty()) throw SqlObjectMapperException("No id column specified")
        return IdValue.build(idColumnNames, valueProvider)
    }
}

/**
 * This class is used to represent id value(s) of an entity.
 */
class IdValue
private constructor(
    val values: List<Any?>,
    private val hashCode: Int
) {
    companion object {
        @JvmStatic
        fun build(idColumns: Collection<String>, valueProvider: ValueProvider): IdValue? {
            var hashCode = 1
            val PRIME = 59
            val values = ArrayList<Any?>(idColumns.size)

            var allNull = true
            for (col in idColumns) {
                val colValue = valueProvider(col)
                if (colValue != null) {
                    allNull = false
                    hashCode = hashCode * PRIME + colValue.hashCode()
                }
                else {
                    hashCode = hashCode * PRIME
                }
                values.add(colValue)
            }
            if (allNull) {
                return null
            }
            else {
                return IdValue(values, hashCode)
            }
        }
    }

    override fun hashCode(): Int {
        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is IdValue && other.values.size == values.size) {
            for (i in values.indices) {
                if (values[i] != other.values[i]) {
                    return false
                }
            }
            return true
        }
        else return false
    }
}

