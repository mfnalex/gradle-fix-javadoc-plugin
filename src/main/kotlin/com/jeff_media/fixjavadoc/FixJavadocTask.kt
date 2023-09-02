package com.jeff_media.fixjavadoc

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.javadoc.Javadoc
import java.io.File
import javax.inject.Inject

val REGEX_DUPLICATED_LINK = "(?<firstLink><a .*?>.*?<\\/a>)\\s*\\k<firstLink>".toRegex()
val REGEX_DOUBLE_ANNOTATION = "(?<annotation>@[A-Za-z.]+)\\s+\\k<annotation>\\s+".toRegex()

abstract class FixJavadocTask @Inject constructor(@Input val task: Javadoc) : DefaultTask() {


    @TaskAction
    fun fixJavadoc() {
        println("FixJavadocTask is being applied")
        val directory = task.destinationDir
        if (directory == null) {
            logger.warn("Javadoc destination directory is null for task " + task.name + " in project " + task.project.name + ". Skipping.")
            return
        }

        directory
            .walk()
            .filter { file -> file.extension.lowercase() == "html" }
            .forEach { file -> replace(file) }
    }


    private fun replace(file: File) {
        var content = file.readText()

        if (REGEX_DOUBLE_ANNOTATION.containsMatchIn(content) || REGEX_DUPLICATED_LINK.containsMatchIn(content)) {
            val foundMatches = countMatches(content, REGEX_DOUBLE_ANNOTATION) + countMatches(content, REGEX_DUPLICATED_LINK)
            content = removeDuplicatedLinks(content)
            content = removeDuplicatedAnnotations(content)
            file.writeText(content)
            println("Removed $foundMatches duplicate annotations in ${file.name}")
        }
    }

    private fun countMatches(input: String, regex: Regex): Int {
        return regex.findAll(input).count()
    }

    private fun removeDuplicatedLinks(input: String): String {
        return input.replace(REGEX_DUPLICATED_LINK, "\${firstLink}")
    }

    private fun removeDuplicatedAnnotations(input: String): String {
        return input.replace(REGEX_DOUBLE_ANNOTATION, "\${annotation} ")
    }


}