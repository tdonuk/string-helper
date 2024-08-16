plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.10.1"
}

group = "github.tdonuk"
version = "1.0.7"

repositories {
    mavenCentral()
}

dependencies {
    // Other dependencies
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.2")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java"))

    updateSinceUntilBuild.set(false)
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    signPlugin {
        certificateChain.set(file("chain.crt").readText())
        privateKey.set(file("private.pem").readText())
        password.set(file("password.txt").readText())
    }

    publishPlugin {
        token.set(file("token.txt").readText())
    }
}



