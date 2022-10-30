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

package sqlObjectMapper.annotationProcessing.bean

import sqlObjectMapper.SqlObjectMapperException
import sqlObjectMapper.annotationProcessing.bean.BeanUtil.Companion.findGetter
import sqlObjectMapper.annotationProcessing.bean.BeanUtil.Companion.findSetter
import sqlObjectMapper.*
import sqlObjectMapper.annotationProcessing.*
import sqlObjectMapper.annotationProcessing.Accessor
import sqlObjectMapper.annotationProcessing.GlobalClassInfo
import sqlObjectMapper.annotationProcessing.LocalClassInfo
import sqlObjectMapper.annotationProcessing.OneToManyProperty
import sqlObjectMapper.annotationProcessing.PGetter
import sqlObjectMapper.annotationProcessing.PSetter
import sqlObjectMapper.annotationProcessing.PropertyInfo
import sqlObjectMapper.annotations.*
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

internal class BeanAccessor(
    override val getter: PGetter,
    val setter: PSetter
) : Accessor(getter)

internal class BeanPropertyInfo(
    override val accessor: BeanAccessor,
    valueConverter: ValueConverter
) : PropertyInfo(accessor, valueConverter)

internal class BeanOneToManyProperty(
    val collectionType: Class<*>,
    override val accessor: BeanAccessor,
    valueConverter: ValueConverter,
    elemClassMapping: ClassMapping<*>
) : OneToManyProperty(accessor, valueConverter, elemClassMapping)

internal class LocalBeanInfo<T : Any>(
    private val nameConverter: NameConverter,
    override val clazz: Class<T>
) : LocalClassInfo<T> {
    private val constructor = clazz.getConstructor()

    override val idColumnNames = HashSet<String>()
    override val nonNestedProperties = LinkedHashMap<String, BeanPropertyInfo>()
    override val nestedProperties = ArrayList<Pair<BeanAccessor, LocalClassInfo<*>>>()
    override val oneToOneProperties = ArrayList<Pair<BeanAccessor, GlobalClassInfo<*>>>()
    override val oneToManyProperties = ArrayList<BeanOneToManyProperty>()

    init {
        var curClazz: Class<*> = clazz
        while (curClazz != java.lang.Object::class.java) {
            for (field in curClazz.declaredFields) {
                when (val annotation = findFieldAnnotation(field)) {
                    is MappedProperty -> handleColumnField(annotation, field)
                    is JoinMany -> handleOneToManyField(annotation, field)
                    is Nested -> handleNestedField(annotation, field)
                    is JoinOne -> handleOneToOneField(annotation, field)
                }
            }
            curClazz = curClazz.superclass
        }
    }

    private fun findFieldAnnotation(field: Field): Annotation? {
        val ignore = field.getAnnotation(IgnoredProperty::class.java)
        val mappedProperty = field.getAnnotation(MappedProperty::class.java)
        val oneToMany = field.getAnnotation(JoinMany::class.java)
        val nested = field.getAnnotation(Nested::class.java)
        val oneToOne = field.getAnnotation(JoinOne::class.java)

        if (ignore != null) return null
        else if (mappedProperty != null) return mappedProperty
        else if (oneToMany != null) return oneToMany
        else if (nested != null) return nested
        else if (oneToOne != null) return oneToOne
        else return MappedProperty()
    }

    private fun findBeanProperty(field: Field): BeanAccessor {
        val getter = findGetter(field, field.declaringClass)
        val setter = findSetter(field, field.declaringClass)
        return BeanAccessor(
            { o -> getter.invoke(o) },
            { o, value -> setter.invoke(o, field.type.cast(value)) },
        )
    }

    private fun handleColumnField(mappedProperty: MappedProperty, field: Field) {
        val columnName = if (mappedProperty.name == "")
            nameConverter(field.name)
        else mappedProperty.name

        if (nonNestedProperties.containsKey(columnName)) {
            throw SqlObjectMapperException("Duplicate column name \"${columnName}\" found in ${clazz}")
        }
        if (mappedProperty.isId) {
            idColumnNames.add(columnName)
        }
        nonNestedProperties[columnName] = BeanPropertyInfo(
            findBeanProperty(field),
            mappedProperty.valueConverter.java.getConstructor().newInstance()
        )
    }

    private fun handleOneToManyField(oneToMany: JoinMany, field: Field) {
        if (!(field.type.isAssignableFrom(List::class.java) || field.type.isAssignableFrom(Set::class.java))) {
            throw SqlObjectMapperException("Only super class of type List<T> or Set<T> is supported for the one to many property ${field.name} in ${clazz}")
        }
        val elemClassMapping = GlobalClassInfo(
            LocalBeanInfo(
                nameConverter,
                if (oneToMany.childEntityType == Any::class)
                    (field.genericType as ParameterizedType).actualTypeArguments[0] as Class<*>
                else
                    oneToMany.childEntityType.java
            )
        )
        oneToManyProperties.add(
            BeanOneToManyProperty(
                field.type,
                findBeanProperty(field),
                oneToMany.elemConverter.java.getConstructor().newInstance(),
                elemClassMapping
            )
        )
    }

    // The parameter is unused. It is there for a purely aesthetic reason.
    @Suppress("UNUSED_PARAMETER")
    private fun handleNestedField(nested: Nested, field: Field) {
        nestedProperties.add(
            Pair(
                findBeanProperty(field),
                LocalBeanInfo(nameConverter, field.type)
            )
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleOneToOneField(oneToOne: JoinOne, field: Field) {
        val joinOneMapping = GlobalClassInfo(LocalBeanInfo(nameConverter, field.type))
        if (joinOneMapping.idMapping.idColumnNames.isEmpty()) {
            throw SqlObjectMapperException("${field.type} must have at least 1 id column because JoinOne is used on it")
        }
        oneToOneProperties.add(Pair(findBeanProperty(field), joinOneMapping))
    }

    override fun createObject(colNameToValue: ValueProvider): T {

        val output = constructor.newInstance()

        for ((colName, beanProperty) in nonNestedProperties) {
            beanProperty.accessor.setter(
                output,
                beanProperty.valueConverter.fromDb(colNameToValue(colName))
            )
        }
        for (oneToManyProperty in oneToManyProperties) {
            val collection: Collection<Any?> =
                if (oneToManyProperty.collectionType.isAssignableFrom(List::class.java))
                    ArrayList()
                else if (oneToManyProperty.collectionType.isAssignableFrom(Set::class.java))
                    HashSet()
                else throw SqlObjectMapperException("Unrecognized collection type ${oneToManyProperty.collectionType} in ${clazz}")
            oneToManyProperty.accessor.setter(output, collection)
        }
        for ((accessor, nestedLocalInfo) in nestedProperties) {
            accessor.setter(output, nestedLocalInfo.createObject(colNameToValue))
        }
        for ((accessor, oneToOneInfo) in oneToOneProperties) {
            if (oneToOneInfo.idMapping.getValue(colNameToValue) != null) {
                accessor.setter(output, oneToOneInfo.createObject(colNameToValue))
            }
        }
        return output
    }

}
