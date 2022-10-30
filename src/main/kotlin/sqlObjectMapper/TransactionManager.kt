package sqlObjectMapper

import java.sql.Connection

class TransactionManager {

    companion object {

        /**
         * A convenient function to manage your transaction. [Connection.TRANSACTION_READ_UNCOMMITTED] is used.
         * @param connection an open [Connection] object
         * @param transactionExecution a lambda where queries are executed
         */
        @JvmStatic
        fun <T> executeTransaction(
            connection: Connection,
            transactionExecution: (conn: Connection)-> T
        ) {
            return executeTransaction(connection, Connection.TRANSACTION_READ_UNCOMMITTED, transactionExecution)
        }


        /**
         * A convenient function to manage your transaction
         * @param connection an open [Connection] object
         * @param transactionIsolation a transaction isolation level, as specified in [Connection.setTransactionIsolation]
         * @param transactionExecution a lambda where queries are executed
         */
        @JvmStatic
        fun <T> executeTransaction(
            connection: Connection,
            transactionIsolation: Int,
            transactionExecution: (conn: Connection)-> T
        ) {
            val oldAutoCommitSetting = connection.autoCommit
            val oldTransactionIsolationSetting = connection.transactionIsolation
            try {
                connection.autoCommit = false
                connection.transactionIsolation = transactionIsolation

                transactionExecution.invoke(connection)
                connection.commit()
            }
            catch (e: Exception) {
                connection.rollback()
                throw e
            }
            finally {
                connection.autoCommit = oldAutoCommitSetting
                connection.transactionIsolation = oldTransactionIsolationSetting
            }
        }
    }
}