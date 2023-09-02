package com.jeff_media.fixjavadoc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc


abstract class FixJavadocPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.afterEvaluate {
            tasks
                .withType(Javadoc::class.java)
                .forEach { javadocTask ->
                    addFixJavadocTaskToJavadocTask(this, javadocTask)
                }

            childProjects.forEach {entry ->
                val subproject: Project = entry.value
                apply(subproject)
            }
        }

    }

    private fun addFixJavadocTaskToJavadocTask(project: Project, javadocTask: Javadoc) {
        val fixJavadocTaskName = "fix" + javadocTask.name.uppercaseFirstLetter()
        val createdTask: FixJavadocTask = project.tasks.create(fixJavadocTaskName, FixJavadocTask::class.java, javadocTask)
        createdTask.group = "documentation"
        createdTask.description = "Fixes output of " + javadocTask.name
        javadocTask.finalizedBy(createdTask)
    }

}