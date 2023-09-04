package com.jeff_media.fixjavadoc

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.File
import java.nio.charset.StandardCharsets

private val REGEX_VALID_ANNOTATION_STRING = "@[a-zA-Z0-9._]+"
private val REGEX_VALID_ANNOTATION = REGEX_VALID_ANNOTATION_STRING.toRegex()
private val REGEX_DOUBLE_ANNOTATION_IN_PARAMETER =
    "(?<annotation>(<a .*>)?${REGEX_VALID_ANNOTATION_STRING}(</a>)?)\\s*\\k<annotation>".toRegex()

class FileFixer(private val file: File, private val addNewLineForMethodParams: Boolean) {

    private val document: Document = Jsoup.parse(file, StandardCharsets.UTF_8.name())

    fun fixAll() {
        document.outputSettings().prettyPrint(false)
        fixFieldDetails()
        fixConstructorDetails()
        fixMethodParameters(addNewLineForMethodParams)
        fixMethodReturnTypes()
        file.writeText(document.html(), StandardCharsets.UTF_8)
    }

    private fun fixFieldDetails() {
        val allSignatures: Elements = document.getElementById("field-detail")?.getElementsByClass("member-signature")
            ?: return // No fields
        for (signature in allSignatures) {
            val listOfAnnotations: List<String> =
                collectAnnotations(signature.getElementsByClass("annotations").first())
            val returnTypeElement: Element? = signature.getElementsByClass("return-type").first()
            removeDoubleAnnotations(returnTypeElement, listOfAnnotations)
        }
    }

    private fun fixMethodReturnTypes() {
        val allSignatures: Elements =
            document.getElementById("method-detail")?.getElementsByClass("member-signature") ?: return
        for (signature in allSignatures) {
            //println("Found signature: $signature\n\n\n")
            val listOfAnnotations: List<String> =
                collectAnnotations(signature.getElementsByClass("annotations").first())
            //println("List of annotations: $listOfAnnotations\n\n\n")
            val returnTypeElement: Element? = signature.getElementsByClass("return-type").first()
            //println("Return type element: $returnTypeElement\n\n\n")
            removeDoubleAnnotations(returnTypeElement, listOfAnnotations)
        }
    }

    private fun fixConstructorDetails() {
        val allSignatures: Elements =
            document.getElementById("constructor-detail")?.getElementsByClass("member-signature")
                ?: return // No fields
        fixSignatures(allSignatures)
    }

    private fun fixMethodParameters(addNewLineForMethodParams: Boolean) {
        val allSignatures: Elements = document.getElementById("method-detail")?.getElementsByClass("member-signature")
            ?: return // No fields
        fixSignatures(allSignatures, addNewLineForMethodParams)
    }

    private fun fixSignatures(allSignatures: Elements) {
        fixSignatures(allSignatures, false)
    }

    private fun fixSignatures(allSignatures: Elements, newline: Boolean) {
        for (signature in allSignatures) {
            val parametersElement = signature.getElementsByClass("parameters").first()
            var parametersHtml: String? = parametersElement?.html() ?: continue
            val newlineChar = if (newline) "\n" else ""
            parametersHtml =
                parametersHtml?.replace(REGEX_DOUBLE_ANNOTATION_IN_PARAMETER, "\${annotation}${newlineChar}")
            if (parametersHtml != null) {
                parametersElement.html(parametersHtml)
            }
        }
    }

    private fun getAnnotationRegex(annotation: String): Regex {
        //println("Getting annotation regex for $annotation")
        val escaped = Regex.escape(annotation)
        //println("Escaped: $escaped")
        val retVal = "<a [^>]*?>$escaped</a> ".toRegex()
        //println("Regex: " + retVal.pattern)
        return retVal
    }

    private fun removeDoubleAnnotations(returnTypeElement: Element?, listOfAnnotations: List<String>) {
        if (returnTypeElement == null) {
            return
        }
        var html = returnTypeElement.html()
        //println("HTML: $html\n\n\n")
        for (annotation in listOfAnnotations) {
            //println("Found annotation: " + annotation + "\n\n\n")
            if (!REGEX_VALID_ANNOTATION.matches(annotation)) {
                //println("Invalid annotation: $annotation")
                continue
            } else {
                //println("Valid annotation: $annotation")
                //println("Old HTML 1: $html\n\n\n")
                html = html.replaceFirst(getAnnotationRegex(annotation), "")
                //println("New HTML 2: $html\n\n\n")
                html = html.replace("$annotation ", "")
                //println("New HTML 3: $html\n\n\n")
                returnTypeElement.html(html)
            }
        }

    }

    private fun collectAnnotations(element: Element?): List<String> {
        val list = ArrayList<String>()

        if (element == null) {
            return list
        }

        list.addAll(element.text().split(" "))

        return list
    }

}