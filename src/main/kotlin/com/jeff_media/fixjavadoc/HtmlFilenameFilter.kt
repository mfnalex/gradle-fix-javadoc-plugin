package com.jeff_media.fixjavadoc

import java.io.File
import java.io.FilenameFilter

class HtmlFilenameFilter : FilenameFilter {
    override fun accept(dir: File?, name: String?): Boolean {
        if(name == null) return false
        return name.lowercase().endsWith(".html")
    }
}