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
//        File(file.parent, file.nameWithoutExtension + ".fixed.html").writeText(document.outerHtml(), StandardCharsets.UTF_8)
//        fixFieldDetails()
        file.writeText(document.html(), StandardCharsets.UTF_8)
    }

    private fun fixFieldDetails() {
        println("Processing field details...")
        val allSignatures: Elements? = document.getElementById("field-detail")?.getElementsByClass("member-signature")
        if(allSignatures == null) {
            println("No fields found.")
            return // No fields
        }
        for (signature in allSignatures) {
            println("Found field " + signature.getElementsByClass("element-name").first()?.text())
            val listOfAnnotations: List<String> = collectAnnotations(signature.getElementsByClass("annotations").first())
            println("  Found annotations: $listOfAnnotations")
            val returnTypeElement: Element? = signature.getElementsByClass("return-type").first()
            removeDoubleAnnotations(returnTypeElement, listOfAnnotations)
        }
    }

    private fun fixConstructorDetails() {
        println("Processing constructor details...")
        val allSignatures: Elements? = document.getElementById("constructor-detail")?.getElementsByClass("member-signature")
        if(allSignatures == null) {
            println("No fields found.")
            return // No fields
        }
        for (signature in allSignatures) {
            println("Found field " + signature.getElementsByClass("element-name").first()?.text())
            val parametersElement = signature.getElementsByClass("parameters").first()
            var parametersHtml = parametersElement?.html()
            if(parametersHtml == null) continue
            println("Found inner html: " + parametersHtml)
            println("Regex matches: " + REGEX_DOUBLE_ANNOTATION_IN_PARAMETER.containsMatchIn(parametersHtml))
            parametersHtml = parametersHtml?.replace(REGEX_DOUBLE_ANNOTATION_IN_PARAMETER, "\${annotation}")
            if(parametersHtml != null) {
                parametersElement?.html(parametersHtml)
            }

            //val listOfAnnotations: List<String> = collectAnnotations(signature.getElementsByClass("parameters").first())
            //println("  Found annotations: $listOfAnnotations")
            //val returnTypeElement: Element? = signature.getElementsByClass("return-type").first()
            //removeDoubleAnnotations(returnTypeElement, listOfAnnotations)
        }
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
//        for(annotation in listOfAnnotations) {
//            val annotationElement: Element? = returnTypeElement.getElementsByTag("a").firstOrNull { it.text() == annotation }
//            if(annotationElement == null) {
//                returnTypeElement.text(returnTypeElement.text().replace("$annotation ", ""))
//            } else {
//                annotationElement.remove()
//            }
//        }
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