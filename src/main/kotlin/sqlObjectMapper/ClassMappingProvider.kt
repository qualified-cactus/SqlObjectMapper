package sqlObjectMapper

import annotationProcessing.ClassMapping
import java.util.concurrent.ConcurrentHashMap

typealias NameConverter = (s: String) -> String

@Suppress("UNCHECKED_CAST")
abstract class ClassMappingProvider {
    private val classMappingCache = ConcurrentHashMap<Class<out Any>, Any>()

    abstract fun <T:Any> getClassMappingNonCached(clazz: Class<T>): ClassMapping<T>

    fun <T:Any> getClassMapping(clazz: Class<T>, useCache: Boolean = true): ClassMapping<T> {
        if (useCache) {
            val cache = classMappingCache[clazz];
            if (cache != null) return cache as ClassMapping<T>;
        }
        val classMapping = getClassMappingNonCached(clazz)
        if (useCache) {
            classMappingCache[clazz] = classMapping
        }
        return classMapping
    }
}




