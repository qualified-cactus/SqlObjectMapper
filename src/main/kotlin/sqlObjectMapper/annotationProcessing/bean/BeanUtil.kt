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