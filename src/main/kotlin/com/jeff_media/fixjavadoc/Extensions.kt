package com.jeff_media.fixjavadoc


/**
 * Uppercases the first letter of a string.
 *
 * @return The string with the first letter uppercased
 */
fun String.uppercaseFirstLetter(): String {
    if (this.isEmpty()) return this
    if (this.length == 1) return this.uppercase()
    return this.substring(0, 1).uppercase() + this.substring(1)
}
