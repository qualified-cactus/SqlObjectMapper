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



package sqlObjectMapper.annotations

import sqlObjectMapper.annotationProcessing.JdbcObjectCreator
import kotlin.reflect.KClass
import java.sql.ResultSet
import java.sql.PreparedStatement
/**
 * Indicate that a property is a column value or a parameter value.
 * @property name specify the name of the column
 * @property isId specify if this column is an identifying column,
 * used only when [JoinMany] annotation is present.
 * You can annotate multiple properties as identifying columns in the case when composite key is need.
 * @property valueConverter used to convert value when setting a parameter and getting value from [ResultSet]
 */
@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
annotation class MappedProperty(
    val name: String = "",
    val isId: Boolean = false,
    val valueConverter: KClass<out ValueConverter> = ValueConverter::class,
)


/**
 * Indicate that a property is a collection of one-to-many child objects in a "LEFT JOIN" query
 * Child object must have at least one property annotated as an identifying column.
 *
 * @property elemConverter use [ValueConverter.fromDb] to convert an object before adding it into the collection
 * @property childEntityType specify the entity to be used to parse [ResultSet]'s row
 *
 * @see MappedProperty
 */
@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class JoinMany(
    val elemConverter: KClass<out ValueConverter> = ValueConverter::class,
    val childEntityType: KClass<*> = Any::class
)

/**
 * Indicate that a property is a one-to-one object,
 * which must have at least one property annotated as an identifying column.
 * If all identifying columns are null, then this one-to-one property will be null.
 */
@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class JoinOne

/**
 * Indicate that there are [MappedProperty] and other mapping-annotations-annotated properties
 * nested within this object.
 */
@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Nested

/**
 * Exclude a property from mapping
 */
@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoredProperty


/**
 * Used by [MappedProperty] annotation and [JoinMany] annotation to indicate
 * a value should be converted when getting value from [ResultSet]
 * and setting a parameter in [PreparedStatement].
 *
 * All inheritors of this class must have a no arg constructor, which will be used by this library.
 */
open class ValueConverter {

    /**
     * Convert value taken from [ResultSet.getObject]
     * before passing it into a property setter / constructor parameter.
     */
    open fun fromDb(value: Any?): Any? {
        return value
    }

    /**
     * Convert value from a property's getter
     * before passing that value into [PreparedStatement]
     */
    open fun toDb(jdbcObjectCreator: JdbcObjectCreator, value: Any?): Any? {
        return value
    }
}