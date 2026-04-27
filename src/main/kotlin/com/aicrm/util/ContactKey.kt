package com.aicrm.util

/**
 * Normalizes [contact] so the same person typing phone/email in different formats
 * still groups together for "returning customer" stats.
 */
object ContactKey {
    fun normalize(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val t = raw.trim()
        if (t.isEmpty()) return null
        if ('@' in t) return t.lowercase()
        val digits = t.filter { it.isDigit() }
        if (digits.length >= 8) return digits
        return t.lowercase().replace(Regex("\\s+"), " ")
    }
}
