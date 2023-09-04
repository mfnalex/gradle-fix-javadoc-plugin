package com.jeff_media.fixjavadoc

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.javadoc.Javadoc
import java.io.File
import javax.inject.Inject


abstract class FixJavadoc @Inject constructor(@Input val task: Javadoc) : DefaultTask() {

    /**
     * Whether to add a new line between annotations and method parameters
     */
    @get:Input
    abstract val newLineOnMethodParameters: Property<Boolean>

    /**
     * Whether to keep the original, unfixed javadocs in a folder called "javadoc-original"
     */
    @get:Input
    abstract val keepOriginal: Property<Boolean>

    init {
        newLineOnMethodParameters.convention(true)
        keepOriginal.convention(false)
    }

    @TaskAction
    fun fixJavadoc() {
        val directory = task.destinationDir
        if (directory == null) {
            logger.warn("Javadoc destination directory is null for task " + task.name + " in project " + task.project.name + ". Skipping.")
            return
        }

        if (keepOriginal.get()) {
            val original = File(directory.parent, directory.name + "-original")
            directory.copyRecursively(original, true)
        }

        directory
            .walk()
            .filter { file -> file.extension.lowercase() == "html" }
            .forEach { file -> FileFixer(file, newLineOnMethodParameters.get()).fixAll() }
    }

}