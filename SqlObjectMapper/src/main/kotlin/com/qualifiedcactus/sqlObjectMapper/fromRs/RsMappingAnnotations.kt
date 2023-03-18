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

/**
 * Specify which constructor to use.
 * If constructor has no parameters, bean mode is used.
 * Else, constructor mode is used.
 */
@[Target(AnnotationTarget.CONSTRUCTOR)
Retention(AnnotationRetention.RUNTIME)
MustBeDocumented]
annotation class RsConstructor

/**
 * Map a property to a column.
 *
 * By default, property without mapping annotation
 * is mapped to a column based on the property's name.
 */
@[Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
Retention(AnnotationRetention.RUNTIME)
MustBeDocumented]
annotation class RsColumn(
    /**
     * Name of the column to map.
     */
    val name:String = "",

    /**
     * Specify if the column is an ID of the entity you are going to map.
     * More than 1 columns can be marked as ID columns (composite key).
     * This only need to be used when [RsToMany] is used
     */
    val isId:Boolean = false,

//    /**
//     * A converter class to convert value from [ResultSet.getObject] before assigning it into the property.
//     */
//    val converter: KClass<out RsValueConverter> = RsNoOpConverter::class,
    /**
     * Extractor to be used to extract value from ResultSet into object
     */
    val extractor: KClass<out RsValueExtractor> = DefaultRsValueExtractor::class
)

/**
 * Mark a property as a to-one property.
 * A to-one property's class contains its own columns and nested/to-one/to-many properties.
 * A to-one property's class have to have at least 1 column marked as ID.
 * If id column(s) is null, then the value of the to-one property is null.
 */
@[Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
Retention(AnnotationRetention.RUNTIME)
MustBeDocumented]
annotation class RsToOne

/**
 * Mark a property as a nested property.
 * A nested property's class contains its own columns and nested/to-one/to-many properties.
 */
@[Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
Retention(AnnotationRetention.RUNTIME)
MustBeDocumented]
annotation class RsNested

/**
 * Mark a property as a to-many collection. Use this when mapping values from the result of a JOIN statement.
 * Must be of type [List] or [Set] with only one generic type parameter.
 */
@[Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
Retention(AnnotationRetention.RUNTIME)
MustBeDocumented]
annotation class RsToMany(
    /**
     * A converter class to convert a value of type [classToMap] (if set) or before adding it into the collection.
     * Conversion result must not be null
     */
    val elementConverter: KClass<out RsElementConverter> = RsElementConverter::class,

    /**
     * Specify a child entity class to map.
     */
    val classToMap: KClass<out Any> = Any::class,
)

/**
 * Specify a property to ignore. Used only in bean mode.
 */
@[Target(AnnotationTarget.FIELD)
Retention(AnnotationRetention.RUNTIME)
MustBeDocumented]
annotation class RsIgnore




