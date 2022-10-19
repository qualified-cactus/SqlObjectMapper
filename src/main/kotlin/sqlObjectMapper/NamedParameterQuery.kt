package sqlObjectMapper

import java.util.concurrent.atomic.AtomicInteger
import java.lang.Character.*

class NamedParameterQuery(npSqlQuery: String) {
    val PARAM_CHARACTER = ':'

    val translatedQuery: String
    val parameterIndexes: Map<String, List<Int>>

    init {
        val translatedQueryBuilder = StringBuilder()

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