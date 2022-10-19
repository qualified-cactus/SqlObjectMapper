package javaImpl

import sqlObjectMapper.SqlObjectMapperException
import annotationProcessing.*
import javaImpl.BeanUtil.Companion.findGetter
import javaImpl.BeanUtil.Companion.findSetter
import sqlObjectMapper.*
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

internal class LocalBeanInfo<T:Any>(
    private val nameConverter: NameConverter,
    override val clazz: Class<T>
) : LocalClassInfo<T> {


    override val idColumnNames = HashSet<String>()
    override val nonNestedProperties = HashMap<String, BeanPropertyInfo>()
    override val nestedProperties = ArrayList<Pair<BeanAccessor, LocalClassInfo<*>>>()
    override val oneToOneProperties = ArrayList<Pair<BeanAccessor, GlobalClassInfo<*>>>()
    override val oneToManyProperties = ArrayList<BeanOneToManyProperty>()

    init {
        var curClazz: Class<*> = clazz
        while (curClazz != java.lang.Object::class.java) {
            for (field in curClazz.declaredFields) {
                when (val annotation = findFieldAnnotation(field)) {
                    is Column -> handleColumnField(annotation, field)
                    is LeftJoinedMany -> handleOneToManyField(annotation, field)
                    is Nested -> handleNestedField(annotation, field)
                    is LeftJoinedOne -> handleOneToOneField(annotation, field)
                }
            }
            curClazz = curClazz.superclass
        }
    }

    private fun findFieldAnnotation(field: Field): Annotation? {
        val ignore = field.getAnnotation(IgnoreProperty::class.java)
        val column = field.getAnnotation(Column::class.java)
        val oneToMany = field.getAnnotation(LeftJoinedMany::class.java)
        val nested = field.getAnnotation(Nested::class.java)
        val oneToOne = field.getAnnotation(LeftJoinedOne::class.java)

        if (ignore != null) return null
        else if (column != null) return column
        else if (oneToMany != null) return oneToMany
        else if (nested != null) return nested
        else if (oneToOne != null) return oneToOne
        else return Column()
    }

    private fun findBeanProperty(field: Field): BeanAccessor {
        val getter = findGetter(field, field.declaringClass)
        val setter = findSetter(field, field.declaringClass)
        return BeanAccessor(
            { o -> getter.invoke(o) },
            { o, value -> setter.invoke(o, field.type.cast(value)) },
        )
    }

    private fun handleColumnField(column: Column, field: Field) {
        val columnName = if (column.name == "")
            nameConverter(field.name)
        else column.name

        if (nonNestedProperties.containsKey(columnName)) {
            throw SqlObjectMapperException("Duplicate column name \"${columnName}\" found in ${clazz}")
        }
        if (column.isId) {
            idColumnNames.add(columnName)
        }
        nonNestedProperties[columnName] = BeanPropertyInfo(
            findBeanProperty(field),
            column.valueConverter.java.getConstructor().newInstance()
        )
    }

    private fun handleOneToManyField(oneToMany: LeftJoinedMany, field: Field) {
        if (!(field.type.isAssignableFrom(List::class.java) || field.type.isAssignableFrom(Set::class.java))) {
            throw SqlObjectMapperException("Only super class of type List<T> or Set<T> is supported for the one to many property ${field.name} in ${clazz}")
        }

        oneToManyProperties.add(
            BeanOneToManyProperty(
                field.type,
                findBeanProperty(field),
                oneToMany.elemConverter.java.getConstructor().newInstance(),
                GlobalClassInfo(
                    LocalBeanInfo(
                        nameConverter,
                        (field.genericType as ParameterizedType).actualTypeArguments[0] as Class<*>
                    )
                )
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
    private fun handleOneToOneField(oneToOne: LeftJoinedOne, field: Field) {
        oneToOneProperties.add(
            Pair(
                findBeanProperty(field),
                GlobalClassInfo(LocalBeanInfo(nameConverter, field.type))
            )
        )
    }

    override fun createObject(colNameToValue: ValueProvider): T {

        val output = clazz.getConstructor().newInstance()

        for ((colName, beanProperty) in nonNestedProperties) {
            beanProperty.accessor.setter(output,
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
