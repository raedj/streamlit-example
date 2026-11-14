package com.polyglot.academy.core.ai

/** Server addresses as typed in settings, where a key or a name sometimes lands by mistake. */
object ProviderUrl {
    private val webAddress = Regex("^https?://[^\\s/?#]+(:\\d+)?([/?#]\\S*)?$", RegexOption.IGNORE_CASE)

    fun isWebAddress(url: String): Boolean = webAddress.matches(url.trim())

    /** The host part, for messages ("could not find generativelanguage.googleapis.com"). */
    fun host(url: String): String = url.trim().substringAfter("://").substringBefore('/').substringBefore('?').ifEmpty { url }
}
