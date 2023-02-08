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

import java.util.concurrent.atomic.AtomicInteger

internal object CamelCaseToUpperSnakeCaseConverter {

    fun convert(input: String): String {

        val out = StringBuilder()
        val curIndex = AtomicInteger(0)

        while (curIndex.get() < input.length) {
            if (Character.isDigit(input[curIndex.get()])) {
                parseNumber(out, curIndex, input)
            }
            else {
                parseWord(out, curIndex, input)
            }
        }
        return out.toString()
    }

    private fun parseWord(out: StringBuilder, indexRef: AtomicInteger, input: String) {
        while (indexRef.get() < input.length) {
            out.append(Character.toUpperCase(input[indexRef.get()]))

            if (indexRef.addAndGet(1) < input.length) {
                if (Character.isUpperCase(input[indexRef.get()]) || Character.isDigit(input[indexRef.get()])) {
                    out.append('_')
                    break
                }
            }
        }
    }

    private fun parseNumber(out: StringBuilder, indexRef: AtomicInteger, input: String) {
        while (indexRef.get() < input.length) {
            out.append(Character.toLowerCase(input[indexRef.get()]))

            if (indexRef.addAndGet(1) < input.length) {
                if(!Character.isDigit(input[indexRef.get()])) {
                    out.append('_')
                    break
                }
            }
        }
    }
}