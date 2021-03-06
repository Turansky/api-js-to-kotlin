package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.JS_STRING

private const val ELEMENT_ID = "yfiles.graphml.ElementId"

internal fun generateElementIdUtils(context: GeneratorContext) {
    // language=kotlin
    context[ELEMENT_ID] = """
            @JsName("String")
            external class ElementId
            private constructor() 
            
            inline fun ElementId(source:String):ElementId = 
                source.unsafeCast<ElementId>()
        """.trimIndent()
}

internal fun applyElementIdHacks(source: Source) {
    source.types()
        .filter { it[ID].startsWith("yfiles.graphml") }
        .forEach {
            it.optFlatMap(PROPERTIES)
                .filter { it[NAME].let { it == "id" || it.endsWith("Id") } }
                .filter { it[TYPE] == JS_STRING }
                .forEach { it[TYPE] = ELEMENT_ID }

            it.optFlatMap(METHODS)
                .filter { it.has(RETURNS) }
                .filter { it[NAME].endsWith("Id") }
                .filter { it[RETURNS][TYPE] == JS_STRING }
                .forEach { it[RETURNS][TYPE] = ELEMENT_ID }

            it.optFlatMap(METHODS)
                .plus(it.optFlatMap(CONSTRUCTORS))
                .optFlatMap(PARAMETERS)
                .filter { it[NAME].let { it == "id" || it.endsWith("Id") } }
                .filter { it[TYPE] == JS_STRING }
                .forEach { it[TYPE] = ELEMENT_ID }
        }
}
