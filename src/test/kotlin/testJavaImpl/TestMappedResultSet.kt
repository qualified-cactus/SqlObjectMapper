package testJavaImpl

import sqlObjectMapper.MappedResultSet
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import BeanDto.*
import createConn
import javaImpl.BeanMappingProvider
import org.junit.jupiter.api.AfterAll
import java.sql.Connection

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestMappedResultSet {

    companion object {
        val orderDbSchema = """
            CREATE TABLE placed_order (
                id INTEGER PRIMARY KEY,
                order_name VARCHAR(50) NOT NULL
            );
            
            CREATE TABLE order_item (
                id INTEGER PRIMARY KEY,
                item_name VARCHAR(50) NOT NULL,
                order_id INTEGER REFERENCES placed_order(id)
            );
            
            
            CREATE TABLE target_location (
                id INTEGER PRIMARY KEY,
                address VARCHAR(50) NOT NULL,
                order_id INTEGER REFERENCES placed_order(id)
            );
            
            CREATE TABLE item_supplier (
                id INTEGER PRIMARY KEY,
                supplier_name VARCHAR(50) NOT NULL,
                item_id INTEGER REFERENCES order_item(id)
            );
            """.trimIndent()
        val seedData = """
            INSERT INTO placed_order VALUES (1, 'order 1');
            INSERT INTO order_item VALUES (1, 'item 1', 1);
            INSERT INTO target_location VALUES (1, 'location 1', 1);
            INSERT INTO target_location VALUES (2, 'location 2', 1);

            INSERT INTO order_item VALUES (2, 'item 2', null);

            INSERT INTO placed_order VALUES (2, 'order 2');
            INSERT INTO order_item VALUES (3, 'item 3', 2);
        """.trimIndent()
        lateinit var conn: Connection;

        @BeforeAll
        @JvmStatic
        fun initConn() {
            conn = createConn()
            val stmt = conn.createStatement()
            stmt.execute(orderDbSchema)
            stmt.execute(seedData)
        }

        @AfterAll
        @JvmStatic
        fun closeConn() {
            conn.close()
        }
    }

    @Test
    fun test1() {
        val beanMappingProvider = BeanMappingProvider()
        val stmt = conn.createStatement()
        MappedResultSet(
            stmt.executeQuery(
                """
                SELECT
                    i.id item_id, i.item_name,
                    o.id order_id, o.order_name,
                    l.id location_id, l.address
                FROM order_item i
                LEFT JOIN placed_order o ON i.order_id = o.id
                LEFT JOIN target_location l ON o.id = l.order_id
                ORDER BY i.id ASC, o.id ASC, l.id ASC
                """.trimIndent()
            ),
            beanMappingProvider
        ).use { rs ->
            val result = rs.toList(Order::class.java)
            assertEquals(3, result.size)


            result[0].apply {
                assertEquals(1, this.itemId)
                assertEquals("item 1", this.itemName)
                assertNotNull(this.placedOrder)
                assertEquals(1, this.placedOrder.orderId)
                assertEquals("order 1", this.placedOrder.orderName)
                assertEquals(2, this.placedOrder.locations.size)

                assertEquals(1, this.placedOrder.locations[0].locationId)
                assertEquals("location 1", this.placedOrder.locations[0].address)
                assertEquals(2, this.placedOrder.locations[1].locationId)
                assertEquals("location 2", this.placedOrder.locations[1].address)
            }
            result[1].apply {
                assertEquals(2, this.itemId)
                assertNull(this.placedOrder)
            }
            result[2].apply {
                assertEquals(3, this.itemId)
                assertNotNull(this.placedOrder)
                assertEquals(0, this.placedOrder.locations.size)
            }
        }

    }
}