package com.jeff_media.fixjavadoc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType


abstract class FixJavadocPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.afterEvaluate {
            val rootProject: Project = this@afterEvaluate
            tasks.withType<Javadoc>().all {
                val originalJavadocTask: Javadoc = this@all
                registerFixTask(rootProject, originalJavadocTask)
            }

            subprojects {
                val subproject: Project = this@subprojects
                tasks.withType<Javadoc>().all {
                    val originalJavadocTask: Javadoc = this@all
                    registerFixTask(subproject, originalJavadocTask)
                }
            }
        }
    }

    private fun registerFixTask(project: Project, originalJavadocTask: Javadoc) {
        val createdTask: TaskProvider<FixJavadoc> = project.tasks.register<FixJavadoc>(originalJavadocTask.name + "FixDuplicatedAnnotations") {
            group = "documentation"
            description = "Removes duplicated annotations created by $name"
            dependsOn(originalJavadocTask)
            javadocTask.set(originalJavadocTask)
        }
        originalJavadocTask.finalizedBy(createdTask)

    }
}