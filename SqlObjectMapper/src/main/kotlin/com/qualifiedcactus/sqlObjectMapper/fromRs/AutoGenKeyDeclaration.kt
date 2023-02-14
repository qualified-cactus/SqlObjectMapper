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

@file:[JvmName("AutoGenKeyDeclaration")
Suppress("UNCHECKED_CAST")]
package com.qualifiedcactus.sqlObjectMapper.fromRs

import com.qualifiedcactus.sqlObjectMapper.MappingProvider
import kotlin.reflect.KClass

/**
 * To declare a [DeclaredGeneratedKeys], use classes [SingleAutoGenKey] or [CompositeAutoGenKey]
 */
interface DeclaredGeneratedKeys<KeyType:Any> {
    val columnNames: Array<String>
}

/**
 * Declare a single-column, auto-generated key of type [KeyType].
 * It is recommended to create object of this class only once.
 */
class SingleAutoGenKey<KeyType:Any>(
    val columnName: String,
    val keyType: KClass<KeyType>
) : DeclaredGeneratedKeys<KeyType> {
    constructor(columnName: String, keyType: Class<KeyType>) : this(columnName, keyType.kotlin)

    override val columnNames: Array<String> = arrayOf(columnName)
}

/**
 * Declare that a composite, auto-generated key of type [KeyType].
 * It is recommended to create object of this class only once.
 */
class CompositeAutoGenKey<KeyType:Any>(val dtoKClass: KClass<KeyType>) : DeclaredGeneratedKeys<KeyType> {

    constructor(dtoClass: Class<KeyType>) : this(dtoClass.kotlin)

    override val columnNames: Array<String> = run {
        val clazzMapping = MappingProvider.mapRsClass(dtoKClass)
        val names = ArrayList<String>(clazzMapping.rootMapping.properties.size)
        fun addNames(classMapping: RsClassMapping) {
            classMapping.properties.forEach { property->
                when (property) {
                    is RsClassMapping.SimpleProperty -> names.add(property.name)
                    is RsClassMapping.NestedProperty -> addNames(property.classMapping.rootMapping)
                }
            }
        }
        addNames(clazzMapping.rootMapping)
        names.toArray(arrayOfNulls<String>(names.size))
    }
}

