package javaImpl

import ClassMapping
import Column
import IgnoreProperty
import JdbcObjectCreator
import Nested
import OneToMany
import OneToManyMapping
import PropertyAccessor
import PropertySetter
import SqlObjectMapperException
import ValueConverter
import ValueProvider
import java.lang.reflect.Constructor
import java.lang.reflect.ParameterizedType
import javaImpl.BeanUtil.Companion.findGetter
import javaImpl.BeanUtil.Companion.findSetter

internal data class BeanProperty(
    val getter: PropertyAccessor,
    val setter: PropertySetter,
    val valueConverter: ValueConverter
)

@Suppress("UNCHECKED_CAST")
internal data class BeanOneToManyProperty(
    val collectionType: Class<*>,
    val beanProperty: BeanProperty,
    override val elemClassMapping: JavaBeanClassMapping<out Any>,
    val valueConverter: ValueConverter,
) : OneToManyMapping {

    override fun addToCollection(parent: Any, obj: Any?) {
        val collection = (beanProperty.getter(parent) as MutableCollection<Any?>)
        collection.add(valueConverter.fromDb(obj))
    }
}

internal class JavaBeanClassMapping<T : Any>(val clazz: Class<T>) : ClassMapping<T>  {

    private val constructor: Constructor<T>
    init {
        try {
            constructor = clazz.getConstructor()
        } catch (e: NoSuchMethodException) {
            throw SqlObjectMapperException("${clazz.name} doesn't have a public no arg constructor")
        }

    }

    private val propertyMap = HashMap<String, BeanProperty>()
    private val oneToManyProperties = ArrayList<BeanOneToManyProperty>()
    var idColumnName: String? = null
        private set

    init {
        do {
            var curClazz: Class<*> = clazz
            for (field in clazz.declaredFields) {
                val column = field.getAnnotation(Column::class.java)
                val oneToMany = field.getAnnotation(OneToMany::class.java)
                val nested = field.getAnnotation(Nested::class.java)
                val ignore = field.getAnnotation(IgnoreProperty::class.java)

                if (ignore != null) {
                    continue

                } else if (column != null) {

                    val columnName = if (column.name == "")
                        NameConverter.convertName(field.name)
                    else column.name
                    val getter = findGetter(field, curClazz)
                    val setter = findSetter(field, curClazz)

                    val valueConverter = column.valueConverter.java.getConstructor().newInstance()

                    propertyMap[columnName] = BeanProperty(
                        { o -> getter.invoke(o) },
                        { o, value -> setter.invoke(o, value) },
                        valueConverter
                    )

                    if (column.isId) {
                        if (idColumnName != null) {
                            throw SqlObjectMapperException("More than one ID found in ${clazz.name} or its nested class(s)")
                        }
                        idColumnName = columnName
                    }


                } else if (oneToMany != null) {
                    val getter = findGetter(field, curClazz)
                    val setter = findSetter(field, curClazz)

                    if (!(field.type.isAssignableFrom(List::class.java) || field.type.isAssignableFrom(Set::class.java))) {
                        throw SqlObjectMapperException("Only super class of type List<T> or Set<T> is supported for the one to many property ${field.name} in ${clazz}")
                    }
                    val valueConverter = oneToMany.elemConverter.java.getConstructor().newInstance()

                    oneToManyProperties.add(BeanOneToManyProperty(
                        field.type,
                        BeanProperty(
                            { o -> getter.invoke(o) },
                            { o, value -> setter.invoke(o, value) },
                            ValueConverter()
                        ),
                        JavaBeanClassMapping((field.genericType as ParameterizedType).actualTypeArguments[0] as Class<*>),
                        valueConverter
                    ))

                } else if (nested != null) {
                    val getter = findGetter(field, curClazz)
                    val setter = findSetter(field, curClazz)

                    val nestedBean = JavaBeanClassMapping(field.type)

                    for ((colName, nestedProperty) in nestedBean.propertyMap) {
                        propertyMap[colName] = nestedProperty.copy(
                            getter = {o -> nestedProperty.getter.invoke(getter.invoke(o))},
                            setter = {o, value ->
                                var nestedObj = getter.invoke(o);
                                if (nestedObj == null)
                                    nestedObj = field.type.getConstructor().newInstance()
                                setter.invoke(o, nestedObj)
                                nestedProperty.setter.invoke(nestedObj, value)
                            }
                        )
                    }

                    for (nestedOneToMany in nestedBean.oneToManyMappings) {
                        oneToManyProperties.add(nestedOneToMany.copy(
                            beanProperty = BeanProperty(
                                {o -> nestedOneToMany.beanProperty.getter.invoke(getter.invoke(o))},
                                {o, value ->
                                    var nestedObj = getter.invoke(o);
                                    if (nestedObj == null)
                                        nestedObj = field.type.getConstructor().newInstance()
                                    setter.invoke(o, nestedObj)
                                    nestedOneToMany.beanProperty.setter.invoke(nestedObj, value)
                                },
                                ValueConverter()
                            ),
                        ))
                    }
                } else {
                    val columnName = NameConverter.convertName(field.name)

                    val getter = findGetter(field, curClazz)
                    val setter = findSetter(field, curClazz)
                    propertyMap[columnName] = BeanProperty(
                        { o -> getter.invoke(o) },
                        { o, value -> setter.invoke(o, value) },
                        ValueConverter()
                    )
                }

            }
            curClazz = clazz.superclass

        } while (curClazz != Object::class.java)
    }

    override val idColumn: String
        get() = idColumnName ?: throw SqlObjectMapperException("${clazz} doesn't have an ID column")

    override val oneToManyMappings: List<BeanOneToManyProperty>
        get() = oneToManyProperties

    override fun createObject(colNameToValue: ValueProvider): T {

        val obj = constructor.newInstance()
        for ((colName, property) in propertyMap) {
            property.setter(obj, property.valueConverter.fromDb(colNameToValue(colName)))
        }
        for (oneToManyProperty in oneToManyProperties) {

            if (oneToManyProperty.collectionType.isAssignableFrom(List::class.java)) {
                oneToManyProperty.beanProperty.setter(obj, ArrayList<Any>())
            }
            else if (oneToManyProperty.collectionType.isAssignableFrom(Set::class.java)) {
                oneToManyProperty.beanProperty.setter(obj, HashSet<Any>())
            }
            else throw SqlObjectMapperException("Unexpected collection type in ${clazz}")
        }
        return obj
    }

    override fun getColumnNameValueMap(jdbcObjectCreator: JdbcObjectCreator, o: T): Map<String, Any?> {

        val valueMap = HashMap<String, Any?>()

        for ((colName, property) in propertyMap) {
            valueMap[colName] = property.valueConverter.toDb(jdbcObjectCreator, property.getter(o))
        }
        return valueMap
    }
}

