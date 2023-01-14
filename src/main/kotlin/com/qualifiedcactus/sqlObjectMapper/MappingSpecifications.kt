package com.qualifiedcactus.sqlObjectMapper

import sqlObjectMapper.SqlObjectMapperException
import java.sql.ResultSet
import kotlin.reflect.KClass

internal interface RsClassMapping {
    val clazz: KClass<Any>

    val properties: Array<ClassProperty>
    fun createInstance(properties: Array<Any?>): Any

    val simpleProperties: List<SimpleProperty>
    val nestedProperties: List<NestedProperty>
    val toManyProperties: List<ToManyProperty>

}

internal class RsTopClassMapping(
    val rootClassMapping: RsClassMapping
) {
    /**
     * Map of {propertyName: {propertyInfo, parameterIndex}}
     */
    val simplePropertyNamesDict: Map<String, Pair<SimpleProperty, Int>>? = run {
        val dict = HashMap<String, Pair<SimpleProperty, Int>>()
        rootClassMapping.properties.forEachIndexed {i, property ->
            if (property is SimpleProperty) {
                dict[property.name] = Pair(property, i)
            }
        }
        return@run if (dict.size == rootClassMapping.properties.size) {
            dict
        }
        else {
            null
        }
    }


    val idMapping: IdMapping? = run {
        val idNameList = ArrayList<String>()
        findIdNames(rootClassMapping, idNameList)

        return@run if (idNameList.isEmpty()) {
            null
        }
        else {
            IdMapping(idNameList)
        }
    }
    private fun findIdNames(clazzMapping: RsClassMapping, idNameList: MutableCollection<String>) {
        clazzMapping.simpleProperties.forEach { property ->
            if (property.isId) {
                idNameList.add(property.name)
            }
        }
        clazzMapping.nestedProperties.forEach {nestedProperty->
            findIdNames(nestedProperty.classMapping, idNameList)
        }
    }


    val toManyList: Collection<ToManyProperty> = ArrayList<ToManyProperty>().also { list ->
        findToManyProperty(rootClassMapping, list)
    }
    private fun findToManyProperty(
        clazzMapping: RsClassMapping,
        toManyProperties: MutableCollection<ToManyProperty>
    ) {
        clazzMapping.toManyProperties.forEach { property ->
            toManyProperties.add(property)
        }
        clazzMapping.nestedProperties.forEach { nestedProperty ->
            findToManyProperty(nestedProperty.classMapping, toManyProperties)
        }
    }

    override fun equals(other: Any?): Boolean {
        return rootClassMapping.clazz == other
    }

    override fun hashCode(): Int {
        return rootClassMapping.clazz.hashCode()
    }
}

internal interface ClassProperty

internal class NestedProperty(
    val classMapping: RsClassMapping,
    val nullIfIdNull: Boolean
) : ClassProperty

internal class SimpleProperty(
    val name: String,
    val isId: Boolean,
    val valueConverter: RsToObjectValueConverter
): ClassProperty

internal class ToManyProperty(
    val collectionType: KClass<Any>,
    val elementTopClassMapping: RsTopClassMapping,
    val valueConverter: RsToObjectValueConverter
) : ClassProperty


interface RsToObjectValueConverter {
    fun convert(resultSetValue: Any?): Any?
}

/**
 * Does absolutely fucking nothing. Use as default value for annotation.
 */
class NoOpConverter : RsToObjectValueConverter {
    override fun convert(resultSetValue: Any?): Any? = resultSetValue
}


internal class IdMapping(
    val idColumnNames: List<String>
) {
    /**
     * @return null if all id columns is null
     */
    fun fromResultSet(resultSet: ResultSet): IdValue? {
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
}


internal interface IdValue

internal class SimpleIdValue(private val idValue: Any) : IdValue {
    override fun hashCode(): Int {
        return idValue.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is SimpleIdValue && other.idValue == idValue
    }
}

internal class CompositeIdValue(
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