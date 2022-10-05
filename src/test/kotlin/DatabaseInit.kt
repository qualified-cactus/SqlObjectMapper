import java.sql.Connection
import java.sql.DriverManager
import java.util.*

fun createDb(seedDatabase: (conn: Connection)-> Unit): Connection {
    val connection = DriverManager.getConnection(
        "jdbc:hsqldb:mem:testdb-${UUID.randomUUID()}",
        "SA",
        ""
    )
    seedDatabase(connection)
    return connection
}


