import java.sql.Connection
import java.sql.DriverManager
import java.util.*


fun createConn(): Connection {
    return DriverManager.getConnection(
        "jdbc:hsqldb:mem:testdb-${UUID.randomUUID()}",
        "SA",
        ""
    )
}

//val orderDbSchema = """
//CREATE TABLE placed_order (
//    id INTEGER PRIMARY KEY,
//    order_name VARCHAR(50) NOT NULL
//);
//
//CREATE TABLE order_item (
//    id INTEGER PRIMARY KEY,
//    item_name VARCHAR(50) NOT NULL,
//    order_id INTEGER REFERENCES placed_order(id)
//);
//
//
//CREATE TABLE target_location (
//    id INTEGER PRIMARY KEY,
//    address VARCHAR(50) NOT NULL,
//    order_id INTEGER REFERENCES placed_order(id)
//);
//
//CREATE TABLE item_supplier (
//    id INTEGER PRIMARY KEY,
//    supplier_name VARCHAR(50) NOT NULL,
//    item_id INTEGER REFERENCES order_item(id)
//);
//""".trimIndent()
//
//val orderDbSeed1 = """
//INSERT INTO placed_order VALUES (1, 'order 1');
//INSERT INTO order_item VALUES (1, 'item 1', 1);
//INSERT INTO target_location VALUES (1, 'location 1', 1);
//INSERT INTO target_location VALUES (2, 'location 2', 1);
//
//INSERT INTO order_item VALUES (2, 'item 2', null);
//
//INSERT INTO placed_order VALUES (2, 'order 2');
//INSERT INTO order_item VALUES (3, 'item 3', 2);
//
//
//""".trimIndent()
//
//class OrderDb : AutoCloseable {
//    val conn: Connection
//    init {
//        conn = createDb { conn ->
//            val stmt = conn.createStatement()
//            stmt.execute(orderDbSchema)
////            stmt.execute(orderDbSeed1)
//
//        }
//    }
//
//    override fun close() {
//        conn.close()
//    }
//}