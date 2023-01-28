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