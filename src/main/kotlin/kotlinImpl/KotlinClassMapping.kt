//package kotlinImpl
//
//import ClassMapping
//import sqlObjectMapper.Column
//import IdMapping
//import JdbcObjectCreator
//import sqlObjectMapper.Nested
//import sqlObjectMapper.LeftJoinedMany
//import OneToManyMapping
//import sqlObjectMapper.PropertyAccessor
//import sqlObjectMapper.SqlObjectMapperException
//import sqlObjectMapper.ValueConverter
//import ValueProvider
//import kotlin.reflect.KClass
//import kotlin.reflect.KParameter
//import kotlin.reflect.KProperty1
//import kotlin.reflect.full.*
//
//@Suppress("UNCHECKED_CAST")
//class KotlinClassMapping<T : Any>(override val clazz: Class<T>) : ClassMapping<T> {
//
//    private val kClazz = clazz.kotlin
//    private val primaryConstructor = kClazz.primaryConstructor
//        ?: throw sqlObjectMapper.SqlObjectMapperException("${kClazz} doesn't have primary constructor")
//
//
//    private val colNameToPropertyMap = HashMap<String, PropertyInfo>()
//    private val collectionProperties = ArrayList<CollectionProperty>()
//
//    private val nonNestedParams = ArrayList<ParamInfo>()
//    private val collectionParams = ArrayList<CollectionParam>()
//    private val nestedParams = ArrayList<NestedParam>()
//
//    private val idColumnNames = ArrayList<String>()
//    override val idMapping: IdMapping
//
//    init {
//        val propertyNames = HashMap<String, KProperty1<T, *>>()
//        for (property in kClazz.memberProperties) {
//            propertyNames[property.name] = property
//        }
//
//        fun findProperty(name: String): KProperty1<T, *> {
//            return propertyNames[name]
//                ?: throw sqlObjectMapper.SqlObjectMapperException("Can't find property ${name} in ${kClazz}")
//        }
//
//        for (param in primaryConstructor.parameters) {
//            val column = param.findAnnotation<sqlObjectMapper.Column>()
//            val oneToMany = param.findAnnotation<sqlObjectMapper.LeftJoinedMany>()
//            val nested = param.findAnnotation<sqlObjectMapper.Nested>()
//
//
//            if (column != null) {
//                val columnName = if (column.name == "")
//                    sqlObjectMapper.NameConverter.convertName(param.name!!)
//                else column.name
//
//                val property = findProperty(param.name!!)
//
//                val valueConverter = column.valueConverter.createInstance()
//
//                if (colNameToPropertyMap.containsKey(columnName)) {
//                    throw sqlObjectMapper.SqlObjectMapperException("Duplicate column/parameter name \"${columnName}\" found in ${kClazz}")
//                }
//
//                if (column.isId) {
//                    idColumnNames.add(columnName)
//                }
//
//                colNameToPropertyMap[columnName] = PropertyInfo(
//                    {o -> property.getter(o as T)},
//                    valueConverter
//                )
//                nonNestedParams.add(ParamInfo(
//                    columnName, param, valueConverter
//                ))
//
//
//            } else if (oneToMany != null) {
//
//                val property = findProperty(param.name!!)
//
//                val collectionType = (param.type.classifier as KClass<*>)
//                if (!collectionType.isSuperclassOf(List::class) && !collectionType.isSuperclassOf(Set::class)) {
//                    throw sqlObjectMapper.SqlObjectMapperException(
//                        "Only super class of type List<T> or Set<T> is supported " +
//                                "for the one to many property ${param.name} in ${kClazz}"
//                    )
//                }
//
//                val elementConverter = oneToMany.elemConverter.createInstance()
//
//                collectionParams.add(CollectionParam(param, collectionType, elementConverter))
//                collectionProperties.add(
//                    CollectionProperty(
//                        KotlinClassMapping((param.type.arguments[0].type!!.classifier as KClass<*>).java),
//                        {o -> property.getter(o as T)},
//                        elementConverter
//                ))
//
//
//            } else if (nested != null) {
//                val property = findProperty(param.name!!)
//
//                val nestedClazz = (param.type.classifier as KClass<*>).java
//                val nestedClassMapping = KotlinClassMapping(nestedClazz)
//
//                nestedParams.add(NestedParam(param, nestedClassMapping))
//
//                for (nestedIdColName in nestedClassMapping.idColumnNames) {
//                    idColumnNames.add(nestedIdColName)
//                }
//                for ((colName, nestedPropertyInfo) in nestedClassMapping.colNameToPropertyMap) {
//                    if (colNameToPropertyMap.containsKey(colName)) {
//                        throw sqlObjectMapper.SqlObjectMapperException("Duplicate column/parameter name \"${colName}\" found in ${nestedClazz} nested inside ${clazz}")
//                    }
//
//                    colNameToPropertyMap[colName] = nestedPropertyInfo.copy(
//                        getter = {o -> nestedPropertyInfo.getter(property.getter(o as T)!!)}
//                    )
//                }
//                for (nestedCollectionProperty in nestedClassMapping.collectionProperties) {
//                    collectionProperties.add(nestedCollectionProperty.copy(
//                        getter = {o -> nestedCollectionProperty.getter(property.getter(o as T)!!)}
//                    ))
//                }
//
//            }
//        }
//
//        idMapping = IdMapping(idColumnNames)
//    }
//
//
//    override val oneToManyMappings: List<OneToManyMapping>
//        get() = collectionProperties
//
//    override fun createObject(colNameToValue: ValueProvider): T {
//        val valueMap = HashMap<KParameter, Any?>()
//
//        for (param in nonNestedParams) {
//            valueMap[param.kParam] = param.valueConverter.fromDb(colNameToValue.invoke(param.colName))
//        }
//
//        for (param in collectionParams) {
//            if (param.collectionType.isSuperclassOf(List::class)) {
//                valueMap[param.kParam] = ArrayList<Any?>()
//            }
//            else if (param.collectionType.isSuperclassOf(Set::class)) {
//                valueMap[param.kParam] = HashSet<Any?>()
//            }
//            else throw Error("Internal Error: failed to match collection type")
//        }
//
//        for (param in nestedParams) {
//            valueMap[param.kParam] = param.classMapping.createObject(colNameToValue)
//        }
//
//        return primaryConstructor.callBy(valueMap)
//    }
//
//
//    override fun getColumnNameValueMap(jdbcObjectCreator: JdbcObjectCreator, o: T): Map<String, Any?> {
//
//        val colNameToValueMap = HashMap<String, Any?>()
//
//        for ((colName, propertyInfo) in colNameToPropertyMap) {
//            colNameToValueMap[colName] = propertyInfo.valueConverter.toDb(jdbcObjectCreator, propertyInfo.getter(o))
//        }
//        return colNameToValueMap
//    }
//
//    private data class PropertyInfo(
//        val getter: sqlObjectMapper.PropertyAccessor,
//        val valueConverter: sqlObjectMapper.ValueConverter
//    )
//
//    private data class ParamInfo(
//        val colName: String,
//        val kParam: KParameter,
//        val valueConverter: sqlObjectMapper.ValueConverter,
//    )
//    private data class CollectionParam(
//        val kParam: KParameter,
//        val collectionType: KClass<*>,
//        val elementConverter: sqlObjectMapper.ValueConverter
//    )
//
//    private data class CollectionProperty(
//        override val elemClassMapping: ClassMapping<*>,
//        val getter: sqlObjectMapper.PropertyAccessor,
//        val elementConverter: sqlObjectMapper.ValueConverter
//    ) : OneToManyMapping {
//
//        override fun addToCollection(parent: Any, obj: Any?) {
//            val collection = (getter(parent) as MutableCollection<Any?>)
//            collection.add(elementConverter.fromDb(obj))
//        }
//    }
//
//    private data class NestedParam(
//        val kParam: KParameter,
//        val classMapping: KotlinClassMapping<*>
//    )
//
//}
