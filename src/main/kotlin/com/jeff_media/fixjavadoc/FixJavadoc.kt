package com.jeff_media.fixjavadoc

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.javadoc.Javadoc
import java.io.File
import java.lang.IllegalStateException
import java.nio.charset.StandardCharsets


abstract class FixJavadoc : DefaultTask() {

    @get:Input
    abstract val javadocTask: Property<Javadoc>


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

    /**
     * Whether to remove the "external link" icon from external links
     */
    @get:Input
    abstract val hideExternalLinksIcon: Property<Boolean>

    init {
        newLineOnMethodParameters.convention(true)
        keepOriginal.convention(false)
        hideExternalLinksIcon.convention(false)
    }

    @TaskAction
    fun fixJavadoc() {
        if(!javadocTask.isPresent) {
            didWork = false
            return
        }

        val directory = javadocTask.get().destinationDir
        if (directory == null || !directory.exists()) {
            //logger.warn("Javadoc destination directory is null for task " + task.name + " in project " + task.project.name + ". Skipping.")
            didWork = false
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

        if(hideExternalLinksIcon.get()) {
            val stylesheetFile = File(directory, "stylesheet.css")
            val stylesheet = stylesheetFile.readText(StandardCharsets.UTF_8)
            val toRemove = FixJavadoc::class.java.getResource("/external-links.css")?.readText(StandardCharsets.UTF_8)
            if(toRemove == null) {
                throw IllegalStateException("Could not find external-links.css in resources")
            }
            if(!stylesheet.contains(toRemove)) {
                logger.warn("Could not find external-links.css in stylesheet.css. External links icon will not be hidden.")
            }
            stylesheetFile.writeText(stylesheet.replace(toRemove, ""), StandardCharsets.UTF_8)
        }
    }

}