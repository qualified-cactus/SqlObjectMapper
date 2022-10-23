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

package annotationProcessing

import sqlObjectMapper.SqlObjectMapperException
import sqlObjectMapper.ValueConverter


internal typealias PGetter = (o: Any) -> Any?
internal typealias PSetter = (o: Any, value: Any?) -> Unit

internal open class Accessor(open val getter: PGetter)

internal open class PropertyInfo(
    open val accessor: Accessor,
    open val valueConverter: ValueConverter
)

@Suppress("UNCHECKED_CAST")
internal open class OneToManyProperty(
    open val accessor: Accessor,
    val valueConverter: ValueConverter,
    override val elemClassMapping: ClassMapping<*>
) : OneToManyMapping {
    override fun addToCollection(parent: Any, obj: Any?) {
        val collection = (accessor.getter(parent) as MutableCollection<Any?>)
        collection.add(valueConverter.fromDb(obj))
    }
}

internal interface LocalClassInfo<T : Any> {
    val clazz: Class<T>
    val idColumnNames: Set<String>
    val nonNestedProperties: Map<String, PropertyInfo>
    val nestedProperties: List<Pair<Accessor, LocalClassInfo<*>>>
    val oneToOneProperties: List<Pair<Accessor, GlobalClassInfo<*>>>
    val oneToManyProperties: List<OneToManyProperty>
    fun createObject(colNameToValue: ValueProvider): T
}


@Suppress("UNCHECKED_CAST")
internal class GlobalClassInfo<T : Any>(
    private val topLevelInfo: LocalClassInfo<T>
) : ClassMapping<T> {

    override val clazz: Class<T> get() = topLevelInfo.clazz
    private val paramMapping = HashMap<String, PropertyInfo>()
    private val idColumnNames = HashSet<String>()
    private val _oneToManyMappings = ArrayList<OneToManyProperty>()
    override val oneToManyMappings: List<OneToManyMapping>
        get() = _oneToManyMappings
    override val idMapping: IdMapping

    init {
        mapParams(topLevelInfo, { o -> o })
        mapOneToManyProperties(topLevelInfo, { o -> o })
        idMapping = IdMapping(idColumnNames)
    }

    private fun mapParams(localClassInfo: LocalClassInfo<*>, curGetter: PGetter) {
        for ((colName, propertyInfo) in localClassInfo.nonNestedProperties) {
            paramMapping[colName] = PropertyInfo(
                Accessor({ o -> propertyInfo.accessor.getter(curGetter(o)!!) }),
                propertyInfo.valueConverter
            )
        }
        for (idColName in localClassInfo.idColumnNames) {
            if (idColumnNames.contains(idColName)) {
                throw SqlObjectMapperException("Duplicate id column name \"${idColName}\" found in ${localClassInfo.clazz}")
            }
            idColumnNames.add(idColName)
        }
        for ((accessor, nestedLocalClassInfo) in localClassInfo.nestedProperties) {
            mapParams(nestedLocalClassInfo, { o -> accessor.getter(curGetter(o)!!) })
        }
    }

    private fun mapOneToManyProperties(localClassInfo: LocalClassInfo<*>, curGetter: PGetter) {
        for (oneToManyProperty in localClassInfo.oneToManyProperties) {
            _oneToManyMappings.add(
                OneToManyProperty(
                    Accessor { o -> oneToManyProperty.accessor.getter(curGetter(o)!!) },
                    oneToManyProperty.valueConverter,
                    oneToManyProperty.elemClassMapping
                )
            )
        }
        for ((accessor, nestedLocalClassInfo) in localClassInfo.nestedProperties) {
            mapOneToManyProperties(nestedLocalClassInfo, { o -> accessor.getter(curGetter(o)!!) })
        }
        for ((accessor, oneToOneLocalClassInfo) in localClassInfo.oneToOneProperties) {
            for (oneToManyProperty in oneToOneLocalClassInfo._oneToManyMappings) {
                _oneToManyMappings.add(OneToManyProperty(
                    Accessor { o -> oneToManyProperty.accessor.getter(accessor.getter(curGetter(o)!!)!!) },
                    oneToManyProperty.valueConverter,
                    oneToManyProperty.elemClassMapping
                ))
            }
        }
    }

    override fun createObject(colNameToValue: ValueProvider): T {
        return topLevelInfo.createObject(colNameToValue)
    }

    override fun getColumnNameValueMap(jdbcObjectCreator: JdbcObjectCreator, o: T): Map<String, Any?> {
        val valueMap = HashMap<String, Any?>()
        for ((colName, propertyInfo) in paramMapping) {
            valueMap[colName] = propertyInfo.valueConverter
                .toDb(jdbcObjectCreator, propertyInfo.accessor.getter(o))
        }
        return valueMap
    }
}