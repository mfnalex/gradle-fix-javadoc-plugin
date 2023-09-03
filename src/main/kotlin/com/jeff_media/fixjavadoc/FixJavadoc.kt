package com.jeff_media.fixjavadoc

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.javadoc.Javadoc
import javax.inject.Inject


abstract class FixJavadoc @Inject constructor(@Input val task: Javadoc) : DefaultTask() {


    @TaskAction
    fun fixJavadoc() {
        val directory = task.destinationDir
        if (directory == null) {
            logger.warn("Javadoc destination directory is null for task " + task.name + " in project " + task.project.name + ". Skipping.")
            return
        }

        directory
            .walk()
            .filter { file -> file.extension.lowercase() == "html" }
            .forEach { file -> FileFixer(file).fixAll() }
    }

}