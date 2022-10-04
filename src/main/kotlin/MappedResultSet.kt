import java.sql.ResultSet


@Suppress("UNCHECKED_CAST")
class MappedResultSet(rs: ResultSet) : ResultSet by rs {

    fun <T> getScalar(): T? {
        if (this.next()) {
            return this.getObject(1) as T;
        }
        else {
            return null
        }
    }

    fun  <T : Any>  getFirst(clazz: Class<T>): T? {
        if (this.next()) {
            return SqlObjectMapper.getClassMapping(clazz).createObject(this::getObject)
        }
        else {
            return null;
        }
    }

    fun <T : Any> toList(clazz: Class<T>): List<T>  {
        val classMapping = SqlObjectMapper.getClassMapping(clazz);

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


    private fun <T: Any> toListOneToMany(resultSet: ResultSet, classMapping: ClassMapping<T>): List<T> {
        val idMap = initializeIdMap(classMapping);
        val output = ArrayList<T>();


        fun extractObj(parentIdColumnName: String, curClazzMapping: ClassMapping<*>): Any? {
            val objectMap = idMap[parentIdColumnName]!!;
            val curId = resultSet.getObject(curClazzMapping.idColumn);
            if (curId == null) return null

            var curObj = objectMap[curId];
            if (curObj == null) {
                curObj = curClazzMapping.createObject(resultSet::getObject);
                objectMap[curId] = curObj;
                if (parentIdColumnName == "") {
                    output.add(curObj as T);
                }
            }

            for (oneToMany in curClazzMapping.oneToManyMappings) {
                val oneToManyObj = extractObj(curClazzMapping.idColumn, oneToMany.elemClassMapping);
                val oneToManyId = resultSet.getObject(oneToMany.elemClassMapping.idColumn);

                if (oneToManyObj != null
                    && !idMap[curClazzMapping.idColumn]!!.containsKey(oneToManyId)
                ) {
                    idMap[curClazzMapping.idColumn]!![oneToManyId] = oneToManyObj;
                    oneToMany.addToCollection(curObj, oneToManyObj);
                }
            }

            return curObj;
        }


        while (resultSet.next()) {
            extractObj("", classMapping);
        }
        return output;
    }


    // { parentIdColumnName: { idValue: obj } }
    private fun initializeIdMap(
        clazzMapping: ClassMapping<*>
    ): MutableMap<String, out MutableMap<Any,Any>> {
        val idMap = HashMap<String, HashMap<Any, Any>>();

        fun f1(parentIdColName: String, clazzMapping: ClassMapping<*>) {
            if (idMap.containsKey(parentIdColName)) {
                throw SqlObjectMapperException("Duplicate parent id column name ${parentIdColName}");
            }
            idMap[parentIdColName] = HashMap();

            for (oneToManyMapping in clazzMapping.oneToManyMappings) {
                f1(clazzMapping.idColumn, oneToManyMapping.elemClassMapping);
            }
        }

        f1("", clazzMapping);
        return idMap;
    }
}