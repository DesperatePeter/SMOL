plugins {
    kotlin("jvm")
    application
}

group = "com.wisp"
version = "1.10.1"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":Utilities"))
    implementation(project(":SMOL_Access"))

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.21")
    implementation(project.property("coroutines")!!)

    // Auto-update
    api("org.update4j:update4j:1.5.8")
}

application {
    mainClass.set("updatestager.Main")
}

tasks.withType<JavaExec>().configureEach {
    val directoryOfFilesToAddToManifest = rootDir.resolve("dist/main/app/SMOL")
    this.setArgsString(directoryOfFilesToAddToManifest.absolutePath)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "16"
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}


kotlin.sourceSets.main {
    // List of where your Kotlin source code is, if any.
    kotlin.setSrcDirs(listOf("main/kotlin"))
}