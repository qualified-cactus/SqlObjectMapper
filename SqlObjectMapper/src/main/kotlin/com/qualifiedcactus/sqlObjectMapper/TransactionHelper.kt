@file:JvmName("TransactionHelper")
package com.qualifiedcactus.sqlObjectMapper

import java.sql.Connection


/**
 * A convenient function for wrapping [executor] function in a transaction.
 * Will roll back transaction if any exception is thrown.
 */
@JvmOverloads
fun <T> Connection.executeTransaction(
    isolationLevel: TransactionIsolation = TransactionIsolation.TRANSACTION_SERIALIZABLE,
    executor: Connection.() -> T
): T {

    val oldAutoCommitSetting = this.autoCommit
    val oldTransactionIsolationSetting = this.transactionIsolation
    var exception: Exception? = null
    var result: T? = null
    try {
        this.autoCommit = false
        this.transactionIsolation = isolationLevel.jdbcValue

        result = executor.invoke(this)
        this.commit()
    }
    catch (e: Exception) {
        this.rollback()
        exception = e
    }

    if (exception != null) {
        throw exception
    }
    else {
        this.autoCommit = oldAutoCommitSetting
        this.transactionIsolation = oldTransactionIsolationSetting
        return result!!
    }
}

/**
 * Isolation level of a transaction
 */
enum class TransactionIsolation(val jdbcValue: Int) {
    /**
     *  Dirty reads are prevented. Non-repeatable reads and phantom reads can occur.
     */
    TRANSACTION_READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),

    /**
     * Dirty reads, non-repeatable reads and phantom reads can occur.
     */
    TRANSACTION_READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),

    /**
     * Dirty reads and non-repeatable reads are prevented; phantom reads can occur.
     */
    TRANSACTION_REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),

    /**
     * Dirty reads, non-repeatable reads and phantom reads are prevented
     */
    TRANSACTION_SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE)
}