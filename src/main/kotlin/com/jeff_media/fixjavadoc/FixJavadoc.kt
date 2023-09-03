package com.jeff_media.fixjavadoc

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.javadoc.Javadoc
import java.io.File
import javax.inject.Inject

val REGEX_STRING_VALID_ANNOTATION_NAME = "@[A-Za-z0-9_.]+"
val REGEX_DOUBLE_ANNOTATION_LINKS = "(?<keep><a .*?>${REGEX_STRING_VALID_ANNOTATION_NAME}</a>)\\s*\\k<keep>".toRegex()
val REGEX_DOUBLE_ANNOTATION_IN_PARAMETERS = "(?<keep>${REGEX_STRING_VALID_ANNOTATION_NAME})\\s+\\k<keep>\\s+".toRegex()
val REGEX_DOUBLE_ANNOTATION_BEFORE_RETURN =
    "(?<keep><span class=\"return-type\">)(?<links>(<a .*?>${REGEX_STRING_VALID_ANNOTATION_NAME}</a>)+)".toRegex()

//val REGEX_NBSP_BETWEEN_MODIFIERS_AND_RETURN_TYPE = "(?<before><span class=\"modifiers\">.*?</span>)&nbsp;(?<after><span class=\"return-type\">)".toRegex()
val REGEX_DOUBLE_ANNOTATION_IN_RETURN_TYPE =
    "(?<before><span class=\"annotations\">((.|\\s)*?)(?<annotation>@[A-Za-z0-9_.]+)\\s*?)(?<between><\\/span>(.|\\s)*?<span class=\"return-type\">)(.|\\s)*?\\k<annotation>".toRegex()
val REGEX_REMOVE_USELESS_NBSP = "(?<before></span>)&nbsp;(?<after><span class=\"return-type\">)".toRegex()

abstract class FixJavadoc @Inject constructor(@Input val task: Javadoc) : DefaultTask() {


    @TaskAction
    fun fixJavadoc() {
        val directory = task.destinationDir
        if (directory == null) {
            logger.warn("Javadoc destination directory is null for task " + task.name + " in project " + task.project.name + ". Skipping.")
            return
        }

//        val directory2 = File(directory.parent, "javadoc_original")
//        directory.copyRecursively(directory2)

        directory
            .walk()
            .filter { file -> file.extension.lowercase() == "html" }
            .forEach { file -> FileFixer(file).fixAll() }
            //.forEach { file -> replace(file) }
    }


    private fun replace(file: File) {
        try {
            var content = file.readText()
            //var printOut = verbose.getOrElse(false)

            if (REGEX_DOUBLE_ANNOTATION_IN_PARAMETERS.containsMatchIn(content)
                || REGEX_DOUBLE_ANNOTATION_LINKS.containsMatchIn(content)
                || REGEX_DOUBLE_ANNOTATION_BEFORE_RETURN.containsMatchIn(content)
            //|| REGEX_NBSP_BETWEEN_MODIFIERS_AND_RETURN_TYPE.containsMatchIn(content)
            //|| REGEX_NBSP_BETWEEN_MODIFIERS_AND_RETURN_TYPE.containsMatchIn(content)
            ) {
                println("Removing Duplicated Links")
                content = removeDuplicatedLinks(content)
                println("Removing Duplicated Annotations")
                content = removeDuplicatedAnnotations(content)
                println("Removing Duplicated Annotations Before Return")
                content = removeDuplicatedAnnotationsBeforeReturn(content)
                println("Removing Duplicated Annotations In Return Type")
                //content = removeDuplicatedAnnotationsInReturnType(content)
                println("Removing Useless &nbsp;")
                //content = removeUselessNbsp(content)
                println("Done")
                //content = removeNbspBetweenModifiersAndReturnType(content)
                file.writeText(content)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun removeDuplicatedLinks(input: String): String {
        return input.replace(REGEX_DOUBLE_ANNOTATION_LINKS, "\${keep}")
    }

    private fun removeDuplicatedAnnotations(input: String): String {
        return input.replace(REGEX_DOUBLE_ANNOTATION_IN_PARAMETERS, "\${keep} ")
    }

    private fun removeDuplicatedAnnotationsBeforeReturn(input: String): String {
        return input.replace(REGEX_DOUBLE_ANNOTATION_BEFORE_RETURN, "\${keep}")
    }

    private fun removeDuplicatedAnnotationsInReturnType(input: String): String {
        return input.replace(REGEX_DOUBLE_ANNOTATION_IN_RETURN_TYPE, "\${before}\${between}")
    }

    private fun removeUselessNbsp(input: String): String {
        return input.replace(REGEX_REMOVE_USELESS_NBSP, "\${before}\${after}")
    }

//    private fun removeNbspBetweenModifiersAndReturnType(input: String): String {
//        return input.replace(REGEX_NBSP_BETWEEN_MODIFIERS_AND_RETURN_TYPE, "\${before}\${after}")
//    }


}