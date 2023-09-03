package com.jeff_media.fixjavadoc

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.javadoc.Javadoc
import java.io.File
import javax.inject.Inject

val REGEX_STRING_VALID_ANNOTATION_NAME= "@[A-Za-z_.]+"
val REGEX_DUPLICATED_LINK = "(?<keep><a .*?>${REGEX_STRING_VALID_ANNOTATION_NAME}<\\/a>)\\s*\\k<keep>".toRegex()
val REGEX_DOUBLE_ANNOTATION_IN_PARAMETERS = "(?<keep>${REGEX_STRING_VALID_ANNOTATION_NAME})\\s+\\k<keep>\\s+".toRegex()
val REGEX_DOUBLE_ANNOTATION_BEFORE_RETURN = "(?<keep><span class=\"return-type\">)(?<links>(<a .*?>@[a-zA-Z0-9._]+<\\/a>)+)".toRegex()
val REGEX_NBSP_BETWEEN_MODIFIERS_AND_RETURN_TYPE = "(?<before><span class=\"modifiers\">.*?<\\/span>)&nbsp;(?<after><span class=\"return-type\">)".toRegex()

abstract class FixJavadoc @Inject constructor(@Input val task: Javadoc) : DefaultTask() {

//    @get:Input
//    var silent: Boolean = false


    @TaskAction
    fun fixJavadoc() {
        val directory = task.destinationDir
        if (directory == null) {
            logger.warn("Javadoc destination directory is null for task " + task.name + " in project " + task.project.name + ". Skipping.")
            return
        }

        val directoryOriginal = File(directory.absolutePath + "_original")
        directory.copyRecursively(directoryOriginal, true)

        directory
            .walk()
            .filter { file -> file.extension.lowercase() == "html" }
            .forEach { file -> replace(file) }
    }


    private fun replace(file: File) {
        var content = file.readText()

        if (REGEX_DOUBLE_ANNOTATION_IN_PARAMETERS.containsMatchIn(content)
            || REGEX_DUPLICATED_LINK.containsMatchIn(content)
            || REGEX_DOUBLE_ANNOTATION_BEFORE_RETURN.containsMatchIn(content)) {
            content = removeDuplicatedLinks(content)
            content = removeDuplicatedAnnotations(content)
            content = removeDuplicatedAnnotationsBeforeReturn(content)
            content = removeNbspBetweenModifiersAndReturnType(content)
            file.writeText(content)

        }
    }

    private fun removeDuplicatedLinks(input: String): String {
        return input.replace(REGEX_DUPLICATED_LINK, "\${keep}")
    }

    private fun removeDuplicatedAnnotations(input: String): String {
        return input.replace(REGEX_DOUBLE_ANNOTATION_IN_PARAMETERS, "\${keep} ")
    }

    private fun removeDuplicatedAnnotationsBeforeReturn(input: String): String {
        return input.replace(REGEX_DOUBLE_ANNOTATION_BEFORE_RETURN, "\${keep}")
    }

    private fun removeNbspBetweenModifiersAndReturnType(input: String): String {
        return input.replace(REGEX_NBSP_BETWEEN_MODIFIERS_AND_RETURN_TYPE, "\${before}\${after}")
    }


}