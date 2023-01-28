package com.qualifiedcactus.sqlObjectMapper.fromRs

import com.qualifiedcactus.sqlObjectMapper.SqlObjectMapperException
import com.qualifiedcactus.sqlObjectMapper.fromRs.RsClassMapping.*
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.util.*
import kotlin.reflect.full.isSuperclassOf

internal class RowParser(
    val rs: ResultSet
) {
    val rsMetaData: ResultSetMetaData = rs.metaData

    fun simpleParse(topClazzMapping: RsTopClassMapping): Any {
        val propertyValues = arrayOfNulls<Any?>(topClazzMapping.rootMapping.properties.size)
        for (i in 1..rsMetaData.columnCount) {
            val (simpleProperty, propertyIndex) = topClazzMapping
                .propertyNameDict[rsMetaData.getColumnLabel(i)] ?: continue
            propertyValues[propertyIndex] = simpleProperty
                .valueConverter.convert(rs.getObject(i))
        }
        return topClazzMapping.rootMapping.createInstance(propertyValues)
    }

    fun parseWithNested(topClazzMapping: RsTopClassMapping): ParseWithNestedResult {
        val parsedCollection: MutableList<ParsedCollection> = if (topClazzMapping.toManyList.isEmpty()) {
            Collections.emptyList()
        } else {
            ArrayList(topClazzMapping.toManyList.size)
        }
        val propertiesValues = arrayOfNulls<Any?>(
            topClazzMapping.rootMapping.properties.size
        )

        topClazzMapping.rootMapping.properties.forEachIndexed { i, property ->
            propertiesValues[i] = when (property) {
                is SimpleProperty -> handleSimpleProperty(property)
                is NestedProperty -> handleNestedProperty(property, parsedCollection)
                is ToManyProperty -> handleToManyProperty(property, parsedCollection)
                else -> throw RuntimeException("Unreachable")
            }
        }
        val output = topClazzMapping.rootMapping.createInstance(propertiesValues)
        return ParseWithNestedResult(output, parsedCollection)
    }

    private fun handleSimpleProperty(property: SimpleProperty): Any? {
        return property.valueConverter.convert(rs.getObject(property.name))
    }

    private fun handleNestedProperty(
        nestedProperty: NestedProperty,
        parsedCollection: MutableList<ParsedCollection>
    ): Any? {
        val clazzMapping = nestedProperty.classMapping.rootMapping
        if (nestedProperty.toOne) {
            val idMapping = nestedProperty.classMapping.idMapping
            if (idMapping.noId) {
                throw SqlObjectMapperException("${clazzMapping.clazz} doesn't have id column(s)")
            }
            if (idMapping.fromResultSet(rs) == null) {
                return null
            }
        }

        val propertiesValues = arrayOfNulls<Any?>(
            clazzMapping.properties.size
        )
        clazzMapping.properties.forEachIndexed { i, innerProperty ->
            propertiesValues[i] = when (innerProperty) {
                is SimpleProperty -> handleSimpleProperty(innerProperty)
                is NestedProperty -> handleNestedProperty(innerProperty, parsedCollection)
                is ToManyProperty -> handleToManyProperty(innerProperty, parsedCollection)
                else -> throw RuntimeException("Unreachable")
            }
        }
        return clazzMapping.createInstance(propertiesValues)
    }

    private fun handleToManyProperty(
        toManyProperty: ToManyProperty,
        parsedCollection: MutableList<ParsedCollection>
    ): Any {
        val collection: MutableCollection<Any> =
            if (toManyProperty.collectionType.isSuperclassOf(List::class)) {
                ArrayList()
            } else if (toManyProperty.collectionType.isSuperclassOf(Set::class)) {
                HashSet()
            } else throw SqlObjectMapperException("${toManyProperty.collectionType} is not the supported collection type")

        parsedCollection.add(ParsedCollection(toManyProperty, collection))
        return collection
    }


    class ParsedCollection(
        val info: ToManyProperty,
        val collection: MutableCollection<Any>
    )

    class ParseWithNestedResult(
        val value: Any,
        val toManyCollectionList: List<ParsedCollection>
    )
}

