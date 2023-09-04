plugins {
    kotlin("jvm") version "1.9.0"
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.1.0"
}

group = "com.jeff-media"
version = "1.13"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.16.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

gradlePlugin {
    website.set("https://github.com/mfnalex/gradle-fix-javadoc-plugin")
    vcsUrl.set("https://github.com/mfnalex/gradle-fix-javadoc-plugin")
    plugins {
        create("fixJavadocPlugin") {
            id = "com.jeff-media.fix-javadoc-plugin"
            implementationClass = "com.jeff_media.fixjavadoc.FixJavadocPlugin"
            displayName = "Fix Javadoc Plugin"
            description = "Gradle plugin to remove duplicate Javadoc annotations caused by e.g. jetbrains annotations"
            tags.set(listOf("javadoc", "documentation", "fix double annotations"))
        }
    }
}

publishing {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://repo.jeff-media.com/public")

            credentials {
                username = properties.get("jeffMediaPublicUser") as String
                password = properties.get("jeffMediaPublicPassword") as String
            }
        }
    }
}