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

import com.qualifiedcactus.sqlObjectMapper.toParam.JdbcObjectCreator
import com.qualifiedcactus.sqlObjectMapper.toParam.ParamUuidToByteArrayConverter
import io.mockk.mockkClass
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.*
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RsConverterTest {

    enum class MyEnum {
        Foo, Bar
    }

    @Test
    fun testEnumConverter() {
        val valConverter = RsStringToEnumConverter()
        val result = valConverter.convert("Foo", MyEnum::class)
        assertInstanceOf(MyEnum::class.java, result)
        assertEquals(MyEnum.Foo, result)
    }

    @Test
    fun testUuidConverter() {
        val id = UUID.randomUUID()
        val rsConverter = RsByteArrayToUuidConverter()
        val paramConverter = ParamUuidToByteArrayConverter()


        val byteArray = paramConverter.convert(id, mockkClass(JdbcObjectCreator::class))
        val convertedId = rsConverter.convert(byteArray, UUID::class)
        assertEquals(id, convertedId)
    }
}