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
    val converter: KClass<out ParamValueConverter> = ParamNoOpConverter::class
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

