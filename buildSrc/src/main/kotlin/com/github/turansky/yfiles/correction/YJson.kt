package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.json.firstWithName
import com.github.turansky.yfiles.json.jArray
import com.github.turansky.yfiles.json.jObject
import com.github.turansky.yfiles.json.objects
import org.json.JSONObject

internal fun JSONObject.staticMethod(name: String): JSONObject =
    getJSONArray(J_STATIC_METHODS)
        .firstWithName(name)

internal fun JSONObject.allMethodParameters(): Sequence<JSONObject> =
    (optionalArray(J_METHODS) + optionalArray(J_STATIC_METHODS))
        .optionalArray(J_PARAMETERS)

internal fun JSONObject.methodParameters(
    methodName: String,
    parameterName: String
): Iterable<JSONObject> =
    methodParameters(
        methodName,
        parameterName,
        { true }
    )

internal fun JSONObject.methodParameters(
    methodName: String,
    parameterName: String,
    parameterFilter: (JSONObject) -> Boolean
): Iterable<JSONObject> {
    val result = getJSONArray(J_METHODS)
        .objects { it.getString(J_NAME) == methodName }
        .flatMap {
            it.getJSONArray(J_PARAMETERS)
                .objects { it.getString(J_NAME) == parameterName }
                .filter(parameterFilter)
        }

    require(result.isNotEmpty())
    { "No method parameters found for object: $this, method: $methodName, parameter: $parameterName" }

    return result
}

internal fun JSONObject.method(methodName: String) =
    getJSONArray(J_METHODS)
        .firstWithName(methodName)

internal fun JSONObject.property(name: String): JSONObject =
    getJSONArray(J_PROPERTIES)
        .firstWithName(name)

internal fun JSONObject.addProperty(
    propertyName: String,
    type: String
) {
    getJSONArray(J_PROPERTIES)
        .put(
            mapOf(
                J_NAME to propertyName,
                J_MODIFIERS to listOf(PUBLIC, FINAL, RO),
                J_TYPE to type
            )
        )
}

internal fun JSONObject.changeNullability(nullable: Boolean) =
    changeModifier(CANBENULL, nullable)

internal fun JSONObject.changeOptionality(optional: Boolean) =
    changeModifier(OPTIONAL, optional)

private fun JSONObject.changeModifier(modifier: String, value: Boolean) {
    val modifiers = getJSONArray(J_MODIFIERS)
    val index = modifiers.indexOf(modifier)

    require((index == -1) == value)

    if (value) {
        modifiers.put(modifier)
    } else {
        modifiers.remove(index)
    }
}

internal fun JSONObject.setSingleTypeParameter(
    name: String = "T",
    bound: String? = null
) {
    put(
        J_TYPE_PARAMETERS,
        jArray(
            typeParameter(name, bound)
        )
    )
}

internal fun JSONObject.addFirstTypeParameter(
    name: String,
    bound: String? = null
) {
    val parameters = getJSONArray(J_TYPE_PARAMETERS)
        .toMutableList()

    parameters.add(0, typeParameter(name, bound))

    put(J_TYPE_PARAMETERS, parameters.toList())
}

internal fun JSONObject.setTypeParameters(
    name1: String,
    name2: String
) {
    put(
        J_TYPE_PARAMETERS,
        jArray(
            typeParameter(name1),
            typeParameter(name2)
        )
    )
}

internal fun typeParameter(
    name: String,
    bound: String? = null
): JSONObject =
    jObject(J_NAME to name).apply {
        if (bound != null) {
            put(J_BOUNDS, arrayOf(bound))
        }
    }

internal fun JSONObject.jsequence(name: String): Sequence<JSONObject> =
    getJSONArray(name)
        .asSequence()
        .map { it as JSONObject }

internal fun JSONObject.optJsequence(name: String): Sequence<JSONObject> =
    if (has(name)) {
        jsequence(name)
    } else {
        emptySequence()
    }

internal fun Sequence<JSONObject>.jsequence(name: String): Sequence<JSONObject> =
    flatMap { it.jsequence(name) }

internal fun JSONObject.optionalArray(name: String): Sequence<JSONObject> =
    if (has(name)) {
        jsequence(name)
    } else {
        emptySequence()
    }

internal fun Sequence<JSONObject>.optionalArray(name: String): Sequence<JSONObject> =
    filter { it.has(name) }
        .jsequence(name)

internal val JSONObject.typeParameter: JSONObject
    get() {
        val typeNames = setOf("type", "tType", "itemType")
        return jsequence(J_PARAMETERS)
            .first { it.getString(J_NAME) in typeNames }
    }

internal fun JSONObject.parameter(name: String): JSONObject {
    return jsequence(J_PARAMETERS)
        .first { it.getString(J_NAME) == name }
}

internal val JSONObject.firstParameter: JSONObject
    get() = getJSONArray(J_PARAMETERS)
        .get(0) as JSONObject

internal val JSONObject.secondParameter: JSONObject
    get() = getJSONArray(J_PARAMETERS)
        .get(1) as JSONObject

internal fun JSONObject.addGeneric(generic: String) {
    val type = getString(J_TYPE)
    put(J_TYPE, "$type<$generic>")
}

internal fun JSONObject.addExtendsGeneric(generic: String) {
    val type = getString(J_EXTENDS)
    put(J_EXTENDS, "$type<$generic>")
}
