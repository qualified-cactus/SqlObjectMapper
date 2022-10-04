package javaImpl

import SqlObjectMapperException
import java.lang.reflect.Field
import java.lang.reflect.Method

class BeanUtil {

    companion object {
        // Follow lombok's bean specs: https://projectlombok.org/features/GetterSetter
        @JvmStatic
        fun findGetter(field: Field, clazz: Class<*>): Method {
            val fieldName = field.name
            lateinit var methodName: String
            if (field.type == Boolean::class.javaPrimitiveType) {
                if (fieldName.startsWith("is")) {
                    methodName = fieldName
                } else {
                    methodName = "is${fieldName[0].uppercase()}${fieldName.substring(1)}"
                }
            } else {
                methodName = "get${fieldName[0].uppercase()}${fieldName.substring(1)}"
            }
            try {
                return clazz.getMethod(methodName)
            } catch (e: NoSuchMethodException) {
                throw SqlObjectMapperException("Cannot find public getter for the field ${fieldName} (${methodName}) in class ${clazz.name}")
            }
        }

        @JvmStatic
        fun findSetter(field: Field, clazz: Class<*>): Method {
            val fieldName = field.name
            val methodName = "set${fieldName[0].uppercase()}${fieldName.substring(1)}"
            try {
                return clazz.getMethod(methodName, field.type)
            } catch (e: NoSuchMethodException) {
                throw SqlObjectMapperException("Cannot find public setter for the field ${fieldName} (${methodName}) in class ${clazz.name}")
            }
        }
    }
}