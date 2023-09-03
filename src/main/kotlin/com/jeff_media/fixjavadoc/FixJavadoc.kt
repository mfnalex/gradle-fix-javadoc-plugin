package com.jeff_media.fixjavadoc

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
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


    @TaskAction
    fun fixJavadoc() {
        val directory = task.destinationDir
        if (directory == null) {
            logger.warn("Javadoc destination directory is null for task " + task.name + " in project " + task.project.name + ". Skipping.")
            return
        }

        /*if(keepOriginal.get()) {
            println("Creating backup of original javadoc in " + directory.absolutePath + "_original")
            val directoryOriginal = File(directory.absolutePath + "_original")
            directory.copyRecursively(directoryOriginal, true)
        }*/

        directory
            .walk()
            .filter { file -> file.extension.lowercase() == "html" }
            .forEach { file -> replace(file) }
    }


    private fun replace(file: File) {
        var content = file.readText()
        //var printOut = verbose.getOrElse(false)

        if (REGEX_DOUBLE_ANNOTATION_IN_PARAMETERS.containsMatchIn(content)
            || REGEX_DUPLICATED_LINK.containsMatchIn(content)
            || REGEX_DOUBLE_ANNOTATION_BEFORE_RETURN.containsMatchIn(content)
            || REGEX_NBSP_BETWEEN_MODIFIERS_AND_RETURN_TYPE.containsMatchIn(content)) {
            val numberOfMatches = getNumberOfMatches(content)
            content = removeDuplicatedLinks(content)
            content = removeDuplicatedAnnotations(content)
            content = removeDuplicatedAnnotationsBeforeReturn(content)
            content = removeNbspBetweenModifiersAndReturnType(content)
            file.writeText(content)
            /*if(numberOfMatches == 0) {
                logger.warn("Found no matches in " + file.absolutePath + " but the file was modified. This is a bug. Please report it.")
            } else {
                println("Fixed " + numberOfMatches + " double annotations in " + file.relativeTo(project.rootDir).path)
            }*/
        }
    }

    fun getNumberOfMatches(content: String): Int {
        var numberOfMatches = 0
        if (REGEX_DOUBLE_ANNOTATION_IN_PARAMETERS.containsMatchIn(content)) {
            numberOfMatches += REGEX_DOUBLE_ANNOTATION_IN_PARAMETERS.findAll(content).count()
        }
        if (REGEX_DUPLICATED_LINK.containsMatchIn(content)) {
            numberOfMatches += REGEX_DUPLICATED_LINK.findAll(content).count()
        }
        if (REGEX_DOUBLE_ANNOTATION_BEFORE_RETURN.containsMatchIn(content)) {
            numberOfMatches += REGEX_DOUBLE_ANNOTATION_BEFORE_RETURN.findAll(content).count()
        }
        if (REGEX_NBSP_BETWEEN_MODIFIERS_AND_RETURN_TYPE.containsMatchIn(content)) {
            numberOfMatches += REGEX_NBSP_BETWEEN_MODIFIERS_AND_RETURN_TYPE.findAll(content).count()
        }
        return numberOfMatches
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