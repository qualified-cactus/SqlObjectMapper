package sqlObjectMapper

import annotationProcessing.ClassMapping
import annotationProcessing.IdValue
import annotationProcessing.OneToManyMapping
import java.sql.ResultSet
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Suppress("UNCHECKED_CAST")
class MappedResultSet(
    rs: ResultSet,
    val classMappingProvider: ClassMappingProvider
) : ResultSet by rs {

    fun <T> getScalar(): T? {
        if (this.next()) {
            return this.getObject(1) as T;
        }
        else {
            return null
        }
    }

    fun <T : Any> getFirst(clazz: Class<T>): T? {
        if (this.next()) {
            return classMappingProvider.getClassMapping(clazz).createObject(this::getObject)
        }
        else {
            return null;
        }
    }

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