package com.qualifiedcactus.sqlObjectMapper

import sqlObjectMapper.SqlObjectMapperException
import java.sql.ResultSet
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


fun <T : Any> ResultSet.parseToObject(clazz: Class<T>): T? {
    return this.parseToObject(clazz.kotlin)
}

fun <T : Any> ResultSet.parseToList(clazz: Class<T>): List<T> {
    return this.parseToList(clazz.kotlin)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> ResultSet.parseToObject(clazz: KClass<T>): T? {
    return this.use {
        if (this.next()) {
            val mapping = MappingProvider.mapRsClass(clazz)
            if (mapping.simplePropertyNamesDict != null) {
                return@use parseRowToSimpleObject(this, mapping) as T?
            } else {
                if (mapping.toManyList.isNotEmpty()) {
                    throw SqlObjectMapperException("Invalid use of to-many collection property")
                }
                return@use parseRowToComplexObject(this, mapping.rootClassMapping, Collections.emptyList()) as T?
            }
        } else return@use null
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> ResultSet.parseToList(clazz: KClass<T>): List<T> {
    return this.use {
        val parsedResults = ArrayList<Any>()
        val rootMapping = MappingProvider.mapRsClass(clazz)

        // simple object
        if (rootMapping.simplePropertyNamesDict != null) {
            while (this.next()) {
                parsedResults.add(parseRowToSimpleObject(this, rootMapping))
            }
        }
        // simple object with nested
        else if (rootMapping.toManyList.isEmpty()) {
            while (this.next()) {
                parsedResults.add(
                    parseRowToComplexObject(this, rootMapping.rootClassMapping, Collections.emptyList())!!
                )
            }
        }
        // complex object with to-many collection(s)
        else {
            val idDict = ParentIdDict(rootMapping)
            fun parseRow(parent: Pair<RsTopClassMapping, IdValue>?, curClazzMapping: RsTopClassMapping) {

                val idValue = (curClazzMapping.idMapping ?: throw SqlObjectMapperException(
                    "Cannot find id column in ${curClazzMapping.rootClassMapping.clazz}"
                ))
                    .fromResultSet(this)

                if (idValue == null) return
                var collectionDict = idDict[curClazzMapping, idValue]

                if (collectionDict == null) {
                    val collectionList = if (curClazzMapping.toManyList.isEmpty())
                        Collections.emptyList()
                    else
                        ArrayList<ToManyCollection>(curClazzMapping.toManyList.size)

                    val objectValue = parseRowToComplexObject(
                        this,
                        curClazzMapping.rootClassMapping, collectionList
                    )!!

                    val collectionMap: CollectionDict =
                        if (collectionList.isEmpty()) {
                            Collections.emptyMap()
                        } else {
                            val result = HashMap<RsTopClassMapping, MutableCollection<Any>>()
                            collectionList.forEach {
                                result[it.info.elementTopClassMapping] = it.collection
                            }
                            result
                        }
                    idDict[curClazzMapping, idValue] = collectionMap

                    if (parent == null) {
                        parsedResults.add(objectValue)
                    }
                    else {
                        idDict[parent.first, parent.second]!!
                            .get(curClazzMapping)!!
                            .add(objectValue)
                    }
                }

                curClazzMapping.toManyList.forEach {
                    parseRow(Pair(curClazzMapping, idValue), it.elementTopClassMapping)
                }
            }
            while (this.next()) {
                parseRow(null, rootMapping)
            }
        }
        parsedResults as List<T>
    }
}


private typealias CollectionDict = MutableMap<RsTopClassMapping, MutableCollection<Any>>
private class ParentIdDict(val root: RsTopClassMapping) {
    val idDict = HashMap<RsTopClassMapping, MutableMap<IdValue, CollectionDict>>().also { dict ->
        createDict(dict, root)
    }

    private fun createDict(
        dict: HashMap<RsTopClassMapping, MutableMap<IdValue, CollectionDict>>,
        cur: RsTopClassMapping
    ) {
        if (cur.toManyList.isNotEmpty()) {
            dict[cur] = HashMap()
            cur.toManyList.forEach {
                createDict(dict, it.elementTopClassMapping)
            }
        }
    }

    operator fun get(mapping: RsTopClassMapping, idValue: IdValue): CollectionDict? {
        return idDict[mapping]?.get(idValue)
    }

    operator fun set(mapping: RsTopClassMapping, idValue: IdValue, collectionDict: CollectionDict) {
        idDict[mapping]?.set(idValue, collectionDict)
    }
}

private class ObjectInfo(
    val objectValue: Any,
    val collectionsDict: MutableMap<RsTopClassMapping, MutableCollection<Any>>
)

//private class ToManyCollectionDict(val toManyCollections: List<ToManyCollection>) {
//    val dict = HashMap<RsTopClassMapping, Pair<MutableSet<IdValue>, MutableCollection<Any>>>().also { m ->
//        for (toMany in toManyCollections) {
//            m[toMany.info.elementTopClassMapping] = Pair(HashSet(), toMany.collection)
//        }
//    }
//
//    fun addToCollection(elementClazz: RsTopClassMapping, elemId: IdValue, elemValue: Any) {
//        val (idSet, collection) = dict[elementClazz]!!
//        if (idSet.contains(elemId)) {
//            return
//        }
//        idSet.add(elemId)
//        collection.add(elemValue)
//    }
//}


private fun parseRowToSimpleObject(resultSet: ResultSet, classMapping: RsTopClassMapping): Any {
    val propertiesValues = arrayOfNulls<Any?>(classMapping.rootClassMapping.properties.size)
    val rsMetaData = resultSet.metaData

    for (i in 0 until rsMetaData.columnCount) {
        val (simpleProperty, propertyIndex) = classMapping
            .simplePropertyNamesDict!![rsMetaData.getColumnLabel(i)] ?: continue
        propertiesValues[propertyIndex] = simpleProperty.valueConverter
            .convert(resultSet.getObject(i))
    }
    return classMapping.rootClassMapping.createInstance(propertiesValues)
}

private fun parseRowToComplexObject(
    resultSet: ResultSet,
    classMapping: RsClassMapping,
    toManyCollections: MutableList<ToManyCollection>,
    nullIfIdNull: Boolean = false
): Any? {
    val propertiesValues = arrayOfNulls<Any?>(classMapping.properties.size)
    var idNull = nullIfIdNull

    classMapping.properties.forEachIndexed { i, property ->
        when (property) {
            is SimpleProperty -> {
                resultSet.getObject(property.name).let { columnValue ->
                    propertiesValues[i] = property.valueConverter.convert(columnValue)
                    if (property.isId) {
                        idNull = idNull && (columnValue == null)
                    }
                }
            }
            is NestedProperty -> {
                propertiesValues[i] = parseRowToComplexObject(
                    resultSet,
                    property.classMapping,
                    toManyCollections,
                    property.nullIfIdNull
                )
            }
            is ToManyProperty -> {
                val collection: MutableCollection<Any> =
                    if (property.collectionType.isSubclassOf(Set::class)) {
                        HashSet()
                    } else if (property.collectionType.isSubclassOf(List::class)) {
                        ArrayList()
                    } else throw SqlObjectMapperException("Collection type of supported ${property.collectionType}")
                toManyCollections.add(
                    ToManyCollection(
                        property,
                        collection
                    )
                )
            }
        }
    }
    return if (idNull) null else classMapping.createInstance(propertiesValues)
}


private class ToManyCollection(
    val info: ToManyProperty,
    val collection: MutableCollection<Any>
)

