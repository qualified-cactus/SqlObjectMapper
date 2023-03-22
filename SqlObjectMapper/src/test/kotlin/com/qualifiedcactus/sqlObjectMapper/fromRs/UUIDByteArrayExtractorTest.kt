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

package com.qualifiedcactus.sqlObjectMapper.fromRs

import com.qualifiedcactus.sqlObjectMapper.createDatabaseConnection
import com.qualifiedcactus.sqlObjectMapper.prepareNpStatement
import com.qualifiedcactus.sqlObjectMapper.toParam.Param
import com.qualifiedcactus.sqlObjectMapper.toParam.ParamUuidToByteArraySetter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UUIDByteArrayExtractorTest {

    companion object {
        const val table = """
            CREATE TABLE uuid_extractor_test(
                id INTEGER PRIMARY KEY,
                uuid_byte BLOB
            )
        """
        const val insertStatement = """
            INSERT INTO uuid_extractor_test VALUES (:id, :uuid_byte)
        """
    }

    val connection = createDatabaseConnection()

    init {
        connection.createStatement().use { stmt ->
            stmt.execute(table)
        }
    }


    @Test
    fun testUuidByteArrayExtractor() {
        val dto = Dto(1, UUID.randomUUID())
        connection.prepareNpStatement(insertStatement)
            .setParametersByDto(dto)
            .useExecuteUpdate()

        val storedDto = connection.prepareNpStatement(
            """
            SELECT id, uuid_byte FROM uuid_extractor_test WHERE id = :id
        """
        ).setParameter("id", 1)
            .useExecuteQuery { it.toObject(Dto::class) }!!

        assertEquals(dto, storedDto)

    }

    data class Dto(
        val id: Int,
        @RsColumn(name = "uuid_byte", extractor = RsByteArrayToUuidExtractor::class)
        @Param(name = "uuid_byte", paramSetter = ParamUuidToByteArraySetter::class)
        val uuid: UUID
    )
}