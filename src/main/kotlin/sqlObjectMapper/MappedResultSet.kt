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

package sqlObjectMapper

import annotationProcessing.ClassMapping
import annotationProcessing.IdValue
import annotationProcessing.OneToManyMapping
import java.sql.ResultSet
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class MappedResultSet(
    rs: ResultSet,
    val classMappingProvider: ClassMappingProvider
) : ResultSet by rs {

    /**
     * Return the first column value of the first row, or null of no row is found
     */
    fun <T> getScalar(): T? {
        if (this.next()) {
            return this.getObject(1) as T;
        }
        else {
            return null
        }
    }

    /**
     * Parse values of the first row into an object of the specified class and return that object.
     * Return null if there is no row.
     */
    fun <T : Any> getFirst(clazz: Class<T>): T? {
        if (this.next()) {
            return classMappingProvider.getClassMapping(clazz).createObject(this::getObject)
        }
        else {
            return null;
        }
    }

    /**
     * Parse rows of ResultSet into a collection whose element type is the specified class.
     */
    fun <T : Any> toList(clazz: Class<T>): List<T>  {
        val classMapping = classMappingProvider.getClassMapping(clazz);

        if (classMapping.oneToManyMappings.isEmpty()) {
            return toListFlat(this, classMapping);
        }
        else {
            return toListOneToMany(this, classMapping);
        }
    }

    private fun <T: Any> toListFlat(resultSet: ResultSet, clazzMapping: ClassMapping<T>): List<T>  {
        val output = ArrayList<T>();
        while (resultSet.next()) {
            output.add(clazzMapping.createObject(resultSet::getObject));
        }
        return output;
    }

    private class ParentObj(
        val clazzMapping: ClassMapping<*>,
        val obj: Any,
        val curLeftJoinedMany: OneToManyMapping
    )

    private fun <T: Any> toListOneToMany(resultSet: ResultSet, classMapping: ClassMapping<T>): List<T> {
        val idMap = initIdMap(classMapping);
        val output = ArrayList<T>();

        fun extractObj(
            parentObj: ParentObj?,
            curClassMapping: ClassMapping<*>
        ) {
            val curObjectIdValue = curClassMapping.idMapping.getValue(resultSet::getObject)

           // object doesn't exist on this row, ignore
            if (curObjectIdValue == null) {
                return
            }


            val parentIdColumns = if (parentObj == null)
                ""
            else
                parentObj.clazzMapping.idMapping.combinedNames

            val curObjectIdToValueMap = idMap[parentIdColumns]!![curClassMapping.idMapping.combinedNames]!!

            var curObject = curObjectIdToValueMap[curObjectIdValue]
            if (curObject == null) {
                curObject = curClassMapping.createObject(resultSet::getObject)
                curObjectIdToValueMap[curObjectIdValue] = curObject as Any

                if (parentObj == null) {
                    output.add(curObject as T)
                }
                else {
                    parentObj.curLeftJoinedMany.addToCollection(parentObj.obj, curObject)
                }
            }

            for (leftJoinedMany in curClassMapping.oneToManyMappings) {
                extractObj(ParentObj(curClassMapping, curObject, leftJoinedMany), leftJoinedMany.elemClassMapping)
            }
        }



        while (resultSet.next()) {
            extractObj(null, classMapping);
        }


        return output;
    }

    // { parentIdColumnName: {idNames: { idValue: obj }} }
    private fun initIdMap(
        clazzMapping: ClassMapping<*>
    ): MutableMap<String, Map<String, MutableMap<IdValue, Any>>> {

        val idMap = HashMap<String, Map<String, MutableMap<IdValue, Any>>>()

        fun f1(parentClazzMapping: ClassMapping<*>) {
            if (parentClazzMapping.oneToManyMappings.size == 1) {
                val elem = parentClazzMapping.oneToManyMappings[0]
                idMap[parentClazzMapping.idMapping.combinedNames] = Collections.singletonMap(
                    elem.elemClassMapping.idMapping.combinedNames, HashMap()
                )
                f1(elem.elemClassMapping)
            }
            else {
                val map = HashMap<String, MutableMap<IdValue, Any>>()
                idMap[parentClazzMapping.idMapping.combinedNames] = map
                for (oneToManyMapping in parentClazzMapping.oneToManyMappings) {
                    map[oneToManyMapping.elemClassMapping.idMapping.combinedNames] = HashMap()
                    f1(oneToManyMapping.elemClassMapping)
                }
            }
        }
        idMap[""] = Collections.singletonMap(clazzMapping.idMapping.combinedNames, HashMap())
        f1(clazzMapping)
        return idMap


    }
}