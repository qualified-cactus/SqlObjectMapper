//package com.qualifiedcactus.sqlObjectMapper
//
//import java.sql.ResultSet
//import kotlin.reflect.KClass
//
//
//class ComparableArray(
//    val elements: Array<Any?>
//) {
//    val computedHashCode: Int
//    init {
//        var hashCode = 1
//        val PRIME = 59
//
//        for (e in elements) {
//            if (e != null) {
//                hashCode = hashCode * PRIME + e.hashCode()
//            }
//            else {
//                hashCode = hashCode * PRIME
//            }
//        }
//        computedHashCode = hashCode
//    }
//
//    override fun hashCode(): Int {
//        return computedHashCode
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (other is ComparableArray) {
//            return elements.contentEquals(other.elements)
//        }
//        else return false
//    }
//}
//
//class IdTracker {
//    lateinit var ids: MutableMap<KClass<out Any>, MutableMap<ComparableArray, Any>>
//}
//
//

//
//fun rsToComplexObject(
//    resultSet: ResultSet,
//    classMapping: RsClassMapping
//): Any {
//    TODO()
//}
//
//class MappedId {
//
//}