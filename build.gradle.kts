plugins {
    kotlin("jvm") version "1.9.0"
    `kotlin-dsl`
    `maven-publish`
}

group = "com.jeff-media"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

gradlePlugin {
    plugins {
        create("fixJavadoc") {
            id = "com.jeff-media.fix-javadoc"
            implementationClass = "com.jeff_media.fixjavadoc.FixJavadocPlugin"
            displayName = "Fix Javadoc Plugin"
            description = "Gradle plugin to remove duplicate Javadoc annotations"
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}