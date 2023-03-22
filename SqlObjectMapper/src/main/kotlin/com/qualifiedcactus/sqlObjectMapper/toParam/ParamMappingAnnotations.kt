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

import kotlin.reflect.KClass
import java.sql.PreparedStatement

/**
 * Map a property to a parameter name.
 * By default, a property without any param mapping annotation
 * is mapped to a parameter based on that property's name
 */
@[Target(AnnotationTarget.FIELD)
Retention(AnnotationRetention.RUNTIME)
MustBeDocumented]
annotation class Param(
    /**
     * Specify the name of the parameter. The name is case-insensitive.
     */
    val name: String = "",
    /**
     * Specify a converter to use to convert the property's value
     * before passing it to [PreparedStatement.setObject(index)][PreparedStatement.setObject]
     */
    val paramSetter: KClass<out ParamValueSetter> = DefaultParamValueSetter::class
)

/**
 * Mark a property as a nested property. A nested property has its properties mapped to parameters
 */
@[Target(AnnotationTarget.FIELD)
Retention(AnnotationRetention.RUNTIME)
MustBeDocumented]
annotation class NestedParams

/**
 * Mark a property to be ignored from passing into statement's parameters.
 */
@[Target(AnnotationTarget.FIELD)
Retention(AnnotationRetention.RUNTIME)
MustBeDocumented]
annotation class IgnoreParam

