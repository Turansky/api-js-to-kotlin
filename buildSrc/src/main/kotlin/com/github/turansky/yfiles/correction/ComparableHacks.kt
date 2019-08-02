package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ICOMPARABLE
import org.json.JSONObject

internal fun applyComparableHacks(source: Source) {
    source.type("IComparable")
        .apply {
            setSingleTypeParameter()
            configureCompareTo("T")
        }

    source.types(
        "ColumnDescriptor",
        "PartitionCellIdEntry",
        "RowDescriptor",
        "SnapResult",
        "SwimlaneDescriptor",
        "TimeSpan",
        "YDimension",
        "YPoint"
    )
        .forEach {
            val id = it.getString(J_ID)

            val implements = it.getJSONArray(J_IMPLEMENTS)
            implements.put(
                implements.indexOf(ICOMPARABLE),
                "$ICOMPARABLE<$id>"
            )

            it.configureCompareTo(id)
        }
}

private fun JSONObject.configureCompareTo(type: String) {
    jsequence(J_METHODS)
        .filter { it.getString(J_NAME) == "compareTo" }
        .singleOrNull()
        ?.apply {
            firstParameter.apply {
                put(J_NAME, "other")
                put(J_TYPE, type)
                changeNullability(false)
            }
        }
}