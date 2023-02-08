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

import kotlin.reflect.KClass

internal interface RsClassMapping {
    val clazz: KClass<*>

    val properties: List<ClassProperty>
    fun createInstance(properties: Array<Any?>): Any

    val simpleProperties: List<SimpleProperty>
    val nestedProperties: List<NestedProperty>
    val toManyProperties: List<ToManyProperty>

    interface ClassProperty

    class SimpleProperty(
        val name: String,
        val isId: Boolean,
        val valueConverter: RsValueConverter
    ) : ClassProperty

    class NestedProperty(
        val classMapping: RsTopClassMapping,
        val toOne: Boolean
    ) : ClassProperty

    class ToManyProperty(
        val collectionType: KClass<*>,
        val elementMapping: RsTopClassMapping,
        val valueConverter: RsValueConverter
    ) : ClassProperty

}



