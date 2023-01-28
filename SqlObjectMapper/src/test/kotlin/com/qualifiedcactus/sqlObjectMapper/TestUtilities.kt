package com.qualifiedcactus.sqlObjectMapper

import java.sql.Connection
import java.sql.DriverManager
import java.util.*

fun createDatabaseConnection(): Connection {
    return DriverManager.getConnection(
        "jdbc:hsqldb:mem:testdb-${UUID.randomUUID()}",
        "SA",
        ""
    )
}