group = "com.yworks.yfiles"
version = "1.0.0-SNAPSHOT"

plugins {
    kotlin("js")
    id("maven-publish")
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":yfiles-kotlin"))
}

val kotlinSourceDir: File
    get() = kotlin
        .sourceSets
        .get("main")
        .kotlin
        .sourceDirectories
        .singleFile

tasks {
    clean {
        doLast {
            delete("src", "out")
        }
    }

    val generateDeclarations by registering {
        doLast {
            val sourceDir = kotlinSourceDir
            delete(sourceDir)

            val apiPath = "https://docs.yworks.com/vsdx-html/assets/api.56a9cdca.js"
            // TODO: implement
            // generateKotlinWrappers(apiPath, sourceDir)
        }
    }

    compileKotlinJs {
        dependsOn(generateDeclarations)
        finalizedBy("publishToMavenLocal")
    }
}

publishing {
    publications {
        register("mavenKotlin", MavenPublication::class) {
            components["kotlin"]
            artifact(tasks.JsSourcesJar.get())
        }
    }
}