package annotationProcessing


class IdMapping(val idColumnNames: Collection<String>) {
    val combinedNames: String
    init {
        if (idColumnNames.isEmpty()) {
            combinedNames = ""
        }
        else {
            val s = StringBuilder()
            for (colName in idColumnNames) {
                s.append(colName).append(';')
            }
            combinedNames = s.toString()
        }

    }

    fun getValue(valueProvider: ValueProvider): IdValue? {
        return IdValue.build(idColumnNames, valueProvider)
    }
}

class IdValue
private constructor(
    val values: List<Any?>,
    private val hashCode: Int
) {
    companion object {
        @JvmStatic
        fun build(idColumns: Collection<String>, valueProvider: ValueProvider): IdValue? {
            var hashCode = 1
            val PRIME = 59
            val values = ArrayList<Any?>()

            var allNull = true
            for (col in idColumns) {
                val colValue = valueProvider(col)
                if (colValue != null) {
                    allNull = false
                    hashCode = hashCode * PRIME + colValue.hashCode()
                }
                values.add(colValue)
            }
            if (allNull) {
                return null
            }
            else {
                return IdValue(values, hashCode)
            }
        }
    }

    override fun hashCode(): Int {
        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is IdValue && other.values.size == values.size) {
            for (i in values.indices) {
                if (values[i] != other.values[i]) {
                    return false
                }
            }
            return true
        }
        else return false
    }
}

