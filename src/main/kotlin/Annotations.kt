import kotlin.reflect.KClass


@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Column(
    val name: String = "",
    val isId: Boolean = false,
    val valueConverter: KClass<out ValueConverter> = ValueConverter::class,
)


@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class OneToMany(
    val elemType: KClass<*> = Any::class,
    val elemConverter: KClass<out ValueConverter> = ValueConverter::class
)


@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Nested
@MustBeDocumented

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreProperty

open class ValueConverter {

    open fun fromDb(value: Any?): Any? {
        return value
    }

    open fun toDb(jdbcObjectCreator: JdbcObjectCreator, value: Any?): Any? {
        return value
    }
}