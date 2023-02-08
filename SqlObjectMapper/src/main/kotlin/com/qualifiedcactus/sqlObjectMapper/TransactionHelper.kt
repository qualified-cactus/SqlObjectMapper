/*
 * MIT License
 *
 * Copyright (c) 2023 qualified-cactus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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