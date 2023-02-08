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
import com.qualifiedcactus.sqlObjectMapper.fromRs.RsClassMapping.SimpleProperty
import com.qualifiedcactus.sqlObjectMapper.fromRs.RsClassMapping.ToManyProperty

internal class RsTopClassMapping(
    val rootMapping: RsClassMapping
) {

    val isSimple = rootMapping.simpleProperties.size == rootMapping.properties.size

    /**
     * Map of {propertyName: {propertyInfo, parameterIndex}}
     */
    val propertyNameDict: Map<String, Pair<SimpleProperty, Int>> by lazy {
        val dict = HashMap<String, Pair<SimpleProperty, Int>>(rootMapping.properties.size)
        rootMapping.properties.forEachIndexed { i, property ->
            if (property is SimpleProperty) {
                dict[property.name] = Pair(property, i)
            }
        }
        dict
    }

    val idMapping: IdMapping by lazy {
        val idNameList = ArrayList<String>()
        rootMapping.simpleProperties.forEach { property ->
            if (property.isId) {
                idNameList.add(property.name)
            }
        }
        rootMapping.nestedProperties.forEach {nestedProperty->
            if (!nestedProperty.toOne && !nestedProperty.classMapping.idMapping.noId) {
                idNameList.addAll(nestedProperty.classMapping.idMapping.idColumnNames)
            }
        }

        IdMapping(idNameList)

    }

    val toManyList: Collection<ToManyProperty> by lazy {
        ArrayList<ToManyProperty>().also { list ->
            rootMapping.toManyProperties.forEach { property ->
                list.add(property)
            }
            rootMapping.nestedProperties.forEach { nestedProperty ->
                list.addAll(nestedProperty.classMapping.toManyList)
            }
        }
    }

//    val straightToMany: Boolean = run {
//        var result = toManyList.size == 1
//        if (!result) return@run false
//        var cur: Collection<ToManyProperty>? = toManyList.first().elementMapping.toManyList
//        while (cur != null) {
//            result = result && cur!!.size <= 1
//            cur = if (cur!!.size == 1) cur!!.first().elementMapping.toManyList else null
//        }
//        result
//    }

    override fun equals(other: Any?): Boolean {
        return rootMapping.clazz == other
    }

    override fun hashCode(): Int {
        return rootMapping.clazz.hashCode()
    }
}