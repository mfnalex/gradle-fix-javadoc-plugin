package com.jeff_media.fixjavadoc

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.File
import java.nio.charset.StandardCharsets

//private val REGEX_DOUBLE_ANNOTATION = "(?<annotation>@[a-zA-Z0-9._]+)\\s+\\k<annotation>".toRegex()
private val REGEX_DOUBLE_ANNOTATION_IN_PARAMETER = "(?<annotation>(<a .*>)?@[a-zA-Z0-9._]+(</a>)?)\\s*\\k<annotation>".toRegex()

class FileFixer(private val file: File) {

    private val document: Document = Jsoup.parse(file, StandardCharsets.UTF_8.name())

    fun fixAll() {
        document.outputSettings().prettyPrint(false)
        fixFieldDetails()
        fixConstructorDetails()
        fixMethodParameters()
        fixMethodReturnTypes()
        file.writeText(document.html(), StandardCharsets.UTF_8)
    }

    private fun fixFieldDetails() {
        val allSignatures: Elements = document.getElementById("field-detail")?.getElementsByClass("member-signature")
            ?: return // No fields
        for (signature in allSignatures) {
            val listOfAnnotations: List<String> = collectAnnotations(signature.getElementsByClass("annotations").first())
            val returnTypeElement: Element? = signature.getElementsByClass("return-type").first()
            removeDoubleAnnotations(returnTypeElement, listOfAnnotations)
        }
    }

    private fun fixMethodReturnTypes() {
        val allSignatures: Elements = document.getElementById("method-detail")?.getElementsByClass("member-signature")
            ?: return // No fields
        for (signature in allSignatures) {
            val listOfAnnotations: List<String> = collectAnnotations(signature.getElementsByClass("annotations").first())
            val returnTypeElement: Element? = signature.getElementsByClass("return-type").first()
            removeDoubleAnnotations(returnTypeElement, listOfAnnotations)
        }
    }

    private fun fixConstructorDetails() {
        val allSignatures: Elements = document.getElementById("constructor-detail")?.getElementsByClass("member-signature")
            ?: return // No fields
        fixSignatures(allSignatures)
    }

    private fun fixSignatures(allSignatures: Elements) {
        for (signature in allSignatures) {
            val parametersElement = signature.getElementsByClass("parameters").first()
            var parametersHtml: String? = parametersElement?.html() ?: continue
            parametersHtml = parametersHtml?.replace(REGEX_DOUBLE_ANNOTATION_IN_PARAMETER, "\${annotation}")
            if (parametersHtml != null) {
                parametersElement.html(parametersHtml)
            }
        }
    }

    private fun fixMethodParameters() {
        val allSignatures: Elements = document.getElementById("method-detail")?.getElementsByClass("member-signature")
            ?: return // No fields
        fixSignatures(allSignatures)
    }

    private fun getAnnotationRegex(annotation: String): Regex {
        return "<a .*?>$annotation</a> ".toRegex()
    }

    private fun removeDoubleAnnotations(returnTypeElement: Element?, listOfAnnotations: List<String>) {
        if(returnTypeElement == null) {
            return
        }
        var html = returnTypeElement.html()
        for(annotation in listOfAnnotations) {
            html = html.replace(getAnnotationRegex(annotation), "")
            html = html.replace("$annotation ", "")
            returnTypeElement.html(html)
        }

    }

    private fun collectAnnotations(element: Element?): List<String> {
        val list = ArrayList<String>()

        if(element == null) {
            return list
        }

        list.addAll(element.text().split(" "))

        return list
    }

}