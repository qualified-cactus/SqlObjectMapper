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

import com.qualifiedcactus.sqlObjectMapper.MappingProvider
import com.qualifiedcactus.sqlObjectMapper.SqlObjectMapperException
import com.qualifiedcactus.sqlObjectMapper.fromRs.IdMapping.*
import com.qualifiedcactus.sqlObjectMapper.fromRs.RowParser.*
import java.sql.ResultSet
import java.util.*
import kotlin.reflect.KClass
import java.sql.SQLException
/**
 * An object used to convert [ResultSet] to desired objects
 */
@Suppress("UNCHECKED_CAST")
object ResultSetParser {

    /**
     * Get columns' values of all rows as a list of DTOs of type [clazz], then close [ResultSet]
     * @throws SqlObjectMapperException on invalid mapping
     * @throws SQLException from JDBC
     */
    @JvmStatic
    fun <T : Any> parseToList(resultSet: ResultSet, clazz: Class<T>): List<T> = parseToList(resultSet, clazz.kotlin)

    /**
     * Get columns' values of the first row as a DTO of type [clazz], then close [ResultSet]
     * @return null if there is no rows
     * @throws SqlObjectMapperException on invalid mapping
     * @throws SQLException from JDBC
     */
    @JvmStatic
    fun <T : Any> parseToObject(resultSet: ResultSet, clazz: Class<T>): T? = parseToObject(resultSet, clazz.kotlin)

    /**
     * Get value of the first column of the first row, then close [ResultSet]
     * @return null if there is no rows
     * @throws SQLException from JDBC
     */
    @JvmStatic
    fun <T : Any> parseToScalar(resultSet: ResultSet): T? {
        return resultSet.use {
            if (resultSet.next()) {
                resultSet.getObject(1) as T?
            } else {
                null
            }
        }
    }

    /**
     * Get columns' values of the first row as a DTO of type [clazz], then close [ResultSet]
     * @return null if there is no rows
     * @throws SqlObjectMapperException on invalid mapping
     * @throws SQLException from JDBC
     */
    internal fun <T : Any> parseToObject(resultSet: ResultSet, clazz: KClass<T>): T? {
        return resultSet.use {
            if (!resultSet.next()) {
                return@use null
            }

            val clazzMapping = MappingProvider.mapRsClass(clazz)
            val rowParser = RowParser(resultSet)

            if (clazzMapping.isSimple) {
                return@use rowParser.simpleParse(clazzMapping) as T
            } else {
                return@use rowParser.parseWithNested(clazzMapping).value as T
            }
        }
    }

    /**
     * Get columns' values of all rows as a list of DTOs of type [clazz], then close [ResultSet]
     * @throws SqlObjectMapperException on invalid mapping
     * @throws SQLException from JDBC
     */
    internal fun <T : Any> parseToList(resultSet: ResultSet, clazz: KClass<T>): List<T> {
        return resultSet.use {
            val clazzMapping = MappingProvider.mapRsClass(clazz)
            val outputList = ArrayList<Any>()
            val rowParser = RowParser(resultSet)

            if (clazzMapping.isSimple) {
                while (resultSet.next()) {
                    outputList.add(rowParser.simpleParse(clazzMapping))
                }
            } else if (clazzMapping.toManyList.isEmpty()) {
                while (resultSet.next()) {
                    outputList.add(rowParser.parseWithNested(clazzMapping).value)
                }
            } else {
                val relationalTracker = RelationalTracker(clazzMapping)

                fun tempF(curMapping: RsTopClassMapping, parent: Pair<IdValue, RsTopClassMapping>?) {
                    if (curMapping.idMapping.noId) {
                        throw SqlObjectMapperException(
                            "${curMapping.rootMapping.clazz} doesn't have id column(s)"
                        )
                    }
                    val curId = curMapping.idMapping.fromResultSet(resultSet)

                    if (curId == null) {
                        return
                    }

                    if (!relationalTracker.objectExists(
                            curMapping.rootMapping.clazz, curId)) {
                        val parsingResult = rowParser.parseWithNested(curMapping)
                        relationalTracker.registerObject(
                            curMapping.rootMapping.clazz,
                            curId,
                            parsingResult
                        )
                        if (parent == null) {
                            outputList.add(parsingResult.value)
                        }
                    }

                    if (parent != null) {
                        val (parentId, parentMapping) = parent
                        relationalTracker.addToParent(
                            ObjectInfo(parentMapping.rootMapping.clazz, parentId),
                            ObjectInfo(curMapping.rootMapping.clazz, curId)
                        )
                    }

                    curMapping.toManyList.forEach {
                        tempF(it.elementMapping, Pair(curId, curMapping))
                    }
                }
                while (resultSet.next()) {
                    tempF(clazzMapping, null)
                }
            }
            outputList as List<T>
        }

    }


    private class ObjectInfo(
        val clazz: KClass<*>,
        val idValue: IdValue
    )

    private class AddedObject(
        val value: Any,
        val collectionsDict: Map<KClass<*>, Pair<MutableSet<IdValue>, ParsedCollection>>
    )

    private class RelationalTracker(
        val topClassMapping: RsTopClassMapping,
    ) {
        private val dict: Map<KClass<*>, MutableMap<IdValue, AddedObject>>

        init {
            dict = HashMap()
            initDict(dict, topClassMapping)
        }

        fun initDict(
            dict: HashMap<KClass<*>, MutableMap<IdValue, AddedObject>>,
            curClazz: RsTopClassMapping
        ) {
            dict[curClazz.rootMapping.clazz] = HashMap()
            curClazz.toManyList.forEach {
                initDict(dict, it.elementMapping)
            }
        }

        fun objectExists(objectType: KClass<*>, objectId: IdValue): Boolean {
            return dict[objectType]!!.containsKey(objectId)
        }

        fun registerObject(clazz: KClass<*>, idValue: IdValue, parsedInfo: ParseWithNestedResult) {
            dict[clazz]!![idValue] = AddedObject(
                parsedInfo.value,
                parsedInfo.toManyCollectionList.let { collectionsList ->
                    val map = HashMap<
                        KClass<*>,
                        Pair<MutableSet<IdValue>, ParsedCollection>
                        >(collectionsList.size)

                    collectionsList.forEach { collection ->
                        map[
                            collection.info.elementMapping
                                .rootMapping.clazz
                        ] = Pair(HashSet(), collection)
                    }
                    map
                }
            )
        }

        /**
         * Parent object and child object must be already added
         */
        fun addToParent(parentInfo: ObjectInfo, objectInfo: ObjectInfo) {
            val parent = dict[parentInfo.clazz]!![parentInfo.idValue]!!
            val (idSet, parsedCollection) = parent.collectionsDict[objectInfo.clazz]!!
            if (!idSet.contains(objectInfo.idValue)) {
                idSet.add(objectInfo.idValue)
                parsedCollection.collection.add(
                    parsedCollection.info.valueConverter.convert(
                        dict[objectInfo.clazz]!![objectInfo.idValue]!!.value
                    )
                        ?: throw SqlObjectMapperException("Element converter is not allowed to convert to null")
                )
            }
        }
    }
}

