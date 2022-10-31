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

import sqlObjectMapper.annotationProcessing.ClassMapping
import java.util.concurrent.ConcurrentHashMap
import sqlObjectMapper.annotationProcessing.bean.BeanMappingProvider
import sqlObjectMapper.annotationProcessing.dataClass.DataClassMappingProvider
typealias NameConverter = (s: String) -> String


/**
 * A base class that all class provider implementations must inherit.
 * @see DataClassMappingProvider
 * @see BeanMappingProvider
 */
@Suppress("UNCHECKED_CAST")
abstract class ClassMappingProvider {
    private val classMappingCache = ConcurrentHashMap<Class<out Any>, Any>()

    abstract fun <T:Any> getClassMappingNonCached(clazz: Class<T>): ClassMapping<T>

    @JvmOverloads
    fun <T:Any> getClassMapping(clazz: Class<T>, useCache: Boolean = true): ClassMapping<T> {
        if (useCache) {
            val cache = classMappingCache[clazz]
            if (cache != null) return cache as ClassMapping<T>
        }
        val classMapping = getClassMappingNonCached(clazz)
        if (useCache) {
            classMappingCache[clazz] = classMapping
        }
        return classMapping
    }

    companion object {
        @JvmStatic
        lateinit var defaultClassMappingProvider: ClassMappingProvider
    }
}




