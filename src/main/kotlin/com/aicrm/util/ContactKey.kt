package com.aicrm.util

/**
 * Normalizes [contact] for 「熟客」 grouping (repeat / 翻買): same business meaning as a unique customer key.
 * - Phone: digits only (e.g. +852 9123 1001 and 85291231001 match).
 * - Email: lowercased full address.
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
