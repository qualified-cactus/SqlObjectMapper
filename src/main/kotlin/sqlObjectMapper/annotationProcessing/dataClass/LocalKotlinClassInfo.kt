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

package sqlObjectMapper.annotationProcessing.dataClass

import sqlObjectMapper.*
import sqlObjectMapper.annotationProcessing.*
import sqlObjectMapper.annotationProcessing.Accessor
import sqlObjectMapper.annotationProcessing.GlobalClassInfo
import sqlObjectMapper.annotationProcessing.LocalClassInfo
import sqlObjectMapper.annotationProcessing.OneToManyProperty
import sqlObjectMapper.annotationProcessing.PGetter
import sqlObjectMapper.annotationProcessing.PropertyInfo
import sqlObjectMapper.annotations.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

internal class KotlinAccessor(
    val constructorParam: KParameter,
    getter: PGetter
) : Accessor(getter)

internal class KotlinProperty(
    override val accessor: KotlinAccessor,
    valueConverter: ValueConverter
) : PropertyInfo(accessor, valueConverter)

internal class KotlinLeftJoinedToManyProperty(
    override val accessor: KotlinAccessor,
    valueConverter: ValueConverter,
    elemClassMapping: ClassMapping<*>
) : OneToManyProperty(accessor, valueConverter, elemClassMapping)

@Suppress("UNCHECKED_CAST")
internal class LocalKotlinClassInfo<T : Any>(
    override val clazz: Class<T>,
    private val nameConverter: NameConverter
) : LocalClassInfo<T> {

    val kClazz = clazz.kotlin
    val constructor = kClazz.primaryConstructor
        ?: throw SqlObjectMapperException("${kClazz} doesn't have primary constructor")

    override val idColumnNames = HashSet<String>()
    override val nonNestedProperties = LinkedHashMap<String, KotlinProperty>()
    override val nestedProperties = ArrayList<Pair<KotlinAccessor, LocalClassInfo<*>>>()
    override val oneToOneProperties = ArrayList<Pair<KotlinAccessor, GlobalClassInfo<*>>>()
    override val oneToManyProperties = ArrayList<KotlinLeftJoinedToManyProperty>()
    private val propertiesNameMap = HashMap<String, KProperty1<T, *>>()

    init {
        for (property in kClazz.memberProperties) {
            propertiesNameMap[property.name] = property
        }

        for (param in constructor.parameters) {
            when (val annotation = findParamAnnotation(param)) {
                is MappedProperty -> handleColumnParam(annotation, param)
                is JoinMany -> handleOneToManyParam(annotation, param)
                is Nested -> handleNestedParam(annotation, param)
                is JoinOne -> handleOneToOneParam(annotation, param)
            }
        }

    }

    private fun findParamAnnotation(param: KParameter): Annotation? {
        val ignore = param.findAnnotation<IgnoredProperty>()
        val mappedProperty = param.findAnnotation<MappedProperty>()
        val oneToMany = param.findAnnotation<JoinMany>()
        val nested = param.findAnnotation<Nested>()
        val oneToOne = param.findAnnotation<JoinOne>()

        if (ignore != null) return null
        else if (mappedProperty != null) return mappedProperty
        else if (oneToMany != null) return oneToMany
        else if (nested != null) return nested
        else if (oneToOne != null) return oneToOne
        else return MappedProperty()
    }

    private fun findAccessor(param: KParameter): KotlinAccessor {
        val property = propertiesNameMap[param.name!!]
            ?: throw SqlObjectMapperException("Can't find property \"${param.name}\" in ${kClazz}")
        return KotlinAccessor(param, { o -> property.getter(o as T) })
    }

    private fun handleColumnParam(annotation: MappedProperty, param: KParameter) {
        val columnName = if (annotation.name == "")
            nameConverter(param.name!!)
        else annotation.name

        if (nonNestedProperties.containsKey(columnName)) {
            throw SqlObjectMapperException("Duplicate column name \"${columnName}\" found in ${clazz}")
        }
        if (annotation.isId) {
            idColumnNames.add(columnName)
        }

        nonNestedProperties[columnName] = KotlinProperty(
            findAccessor(param),
            annotation.valueConverter.createInstance()
        )
    }

    private fun handleOneToManyParam(annotation: JoinMany, param: KParameter) {
        val paramType = param.type.classifier as KClass<*>
        if (!(paramType.isSuperclassOf(List::class) || paramType.isSuperclassOf(Set::class))) {
            throw SqlObjectMapperException("Only super class of type List<T> or Set<T> is supported for the one to many property ${param.name} in ${clazz}")
        }
        val elemClassMapping = GlobalClassInfo(
            LocalKotlinClassInfo(
                if (annotation.childEntityType == Any::class)
                    (param.type.arguments[0].type!!.classifier as KClass<*>).java
                else
                    annotation.childEntityType.java,
                nameConverter
            )
        )
        oneToManyProperties.add(
            KotlinLeftJoinedToManyProperty(
                findAccessor(param),
                annotation.elemConverter.createInstance(),
                elemClassMapping
            )
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleNestedParam(annotation: Nested, param: KParameter) {
        nestedProperties.add(
            Pair(
                findAccessor(param),
                LocalKotlinClassInfo((param.type.classifier as KClass<*>).java, nameConverter)
            )
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleOneToOneParam(annotation: JoinOne, param: KParameter) {
        val joinedClazz = param.type.classifier as KClass<*>
        val nestedMapping = GlobalClassInfo(LocalKotlinClassInfo(joinedClazz.java, nameConverter))
        if (nestedMapping.idMapping.idColumnNames.isEmpty()) {
            throw SqlObjectMapperException("${joinedClazz} must have at least 1 id column because JoinOne is used on it")
        }

        oneToOneProperties.add(Pair(findAccessor(param),nestedMapping))
    }


    override fun createObject(colNameToValue: ValueProvider): T {
        val paramValueMap = HashMap<KParameter, Any?>()

        for ((colName, kotlinProperty) in nonNestedProperties) {
            val value = kotlinProperty.valueConverter.fromDb(colNameToValue(colName))
            paramValueMap[kotlinProperty.accessor.constructorParam] = value
        }

        for (oneToManyProperty in oneToManyProperties) {
            val collectionType = oneToManyProperty.accessor.constructorParam.type.classifier as KClass<*>

            val collection: Collection<Any?> = if (collectionType.isSuperclassOf(List::class)) {
                ArrayList()
            }
            else if (collectionType.isSuperclassOf(Set::class)) {
                HashSet()
            }
            else throw Error("Unreached error")

            paramValueMap[oneToManyProperty.accessor.constructorParam] = collection
        }

        for ((accessor, nestedLocalInfo) in nestedProperties) {
            paramValueMap[accessor.constructorParam] = nestedLocalInfo.createObject(colNameToValue)
        }

        for ((accessor, oneToOneInfo) in oneToOneProperties) {
            paramValueMap[accessor.constructorParam] =
                if (oneToOneInfo.idMapping.getValue(colNameToValue) != null)
                    oneToOneInfo.createObject(colNameToValue)
                else null
        }

        return constructor.callBy(paramValueMap)
    }
}