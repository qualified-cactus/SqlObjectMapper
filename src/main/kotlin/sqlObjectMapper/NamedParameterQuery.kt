/*
 MIT License

 Copyright (c) 2022 qualified-cactus

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package sqlObjectMapper

import java.util.concurrent.atomic.AtomicInteger
import java.lang.Character.*


/**
 * Used to parse sql string with named parameter.
 * Parameter start with the character ':'.
 */
class NamedParameterQuery(npSqlQuery: String) {
    val PARAM_CHARACTER = ':'

    val translatedQuery: String
    val parameterIndexes: Map<String, List<Int>>

    init {
        val translatedQueryBuilder = StringBuilder(npSqlQuery.length)

        val paramIndexes = HashMap<String, MutableList<Int>>()
        val i = AtomicInteger(0)
        val curParameterIndex = AtomicInteger(1)

        while (i.get() < npSqlQuery.length) {
            if (npSqlQuery[i.get()] == '?') {
                throw SqlObjectMapperException("Character '?' is not allowed")
            } else if (npSqlQuery[i.get()] == PARAM_CHARACTER) {
                parseParam(translatedQueryBuilder, i, npSqlQuery, paramIndexes, curParameterIndex)
            } else {
                parseOther(translatedQueryBuilder, i, npSqlQuery)
            }
        }

        translatedQuery = translatedQueryBuilder.toString()
        parameterIndexes = paramIndexes
    }

    private fun parseParam(
        out: StringBuilder, curIndex: AtomicInteger, input: String,
        paramIndexes: MutableMap<String, MutableList<Int>>,
        currentParameterIndex: AtomicInteger
    ) {
        assert(input[curIndex.get()] == PARAM_CHARACTER)
        curIndex.incrementAndGet()

        // check first character of parameter
        val validParameterStart = curIndex.get() < input.length
                && isJavaIdentifierStart(input[curIndex.get()])
        if (!validParameterStart) {
            throw SqlObjectMapperException("Invalid parameter's starting character or parameter is empty")
        }

        val paramNameBuilder = StringBuilder()
        paramNameBuilder.append(input[curIndex.getAndIncrement()])

        // add the rest of parameter's character to builder
        while (
            curIndex.get() < input.length
            && isJavaIdentifierPart(input[curIndex.get()])
        ) {
            paramNameBuilder.append(input[curIndex.getAndIncrement()])
        }

        val paramName = paramNameBuilder.toString()

        // add built param name and index to param index map
        var indexList: MutableList<Int>? = paramIndexes[paramName]
        if (indexList != null) {
            indexList.add(currentParameterIndex.getAndIncrement())
        }
        else {
            indexList = ArrayList()
            indexList.add(currentParameterIndex.getAndIncrement())
            paramIndexes[paramName] = indexList
        }

        // append parameter ? to translated query
        out.append('?')
    }

    private fun parseOther(out: StringBuilder, curIndex: AtomicInteger, input: String) {

        // keep adding until the start of a parameter name is detected
        while (curIndex.get() < input.length) {
            if (input[curIndex.get()] == PARAM_CHARACTER) {
                break
            }
            else {
                out.append(input[curIndex.getAndIncrement()])
            }

        }
    }

}