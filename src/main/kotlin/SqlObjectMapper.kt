import javaImpl.JavaBeanClassMapping
import kotlinImpl.KotlinClassMapping
import java.util.concurrent.ConcurrentHashMap

typealias DefaultNameConverter = (s: String) -> String

@Suppress("UNCHECKED_CAST")
class SqlObjectMapper {

    enum class Mode {
        JavaBean,
        Kotlin
    }

    companion object {

        @JvmStatic
        var mode: Mode = Mode.Kotlin
        @JvmStatic
        var cacheClassMapping: Boolean = false
        @JvmStatic
        var cacheSqlParsing: Boolean = false
        @JvmStatic
        var defaultNameConverter: DefaultNameConverter = NameConverter.Companion::camelCaseToSnakeCase

        private val classMappingCache = ConcurrentHashMap<Class<out Any>, Any>()


        fun <T:Any> getClassMapping(clazz: Class<T>): ClassMapping<T> {
            if (cacheClassMapping) {
                val cache = classMappingCache[clazz];
                if (cache != null) return cache as ClassMapping<T>;
            }

            val mapping = when (mode) {
                Mode.Kotlin -> KotlinClassMapping(clazz)
                Mode.JavaBean -> JavaBeanClassMapping(clazz)
            }
            if (cacheClassMapping) {
                classMappingCache[clazz] = mapping;
            }
            return mapping
        }
    }
}