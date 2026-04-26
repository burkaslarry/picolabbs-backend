package com.aicrm.service

import com.aicrm.repository.RagProductRepository
import com.aicrm.repository.RagServiceRepository
import org.springframework.stereotype.Service

/**
 * Maps inquiry text to a lead [vertical] using RAG product/service [category] slugs
 * (e.g. picolabbs_wellness). Longer name matches win to reduce false positives.
 */
@Service
class RagVerticalMatcher(
    private val ragProductRepository: RagProductRepository,
    private val ragServiceRepository: RagServiceRepository
) {

    fun matchVerticalFromCatalog(rawMessage: String): String? {
        matchInRegion(rawMessage, "hk")?.let { return it }
        matchInRegion(rawMessage, null)?.let { return it }
        return null
    }

    private fun matchInRegion(rawMessage: String, region: String?): String? {
        val text = rawMessage
        val lower = text.lowercase()
        var bestCategory: String? = null
        var bestLen = 0
        fun consider(name: String, category: String?) {
            val c = category?.trim()?.takeIf { it.isNotEmpty() } ?: return
            val n = name.trim()
            if (n.isEmpty()) return
            if (text.contains(n) || lower.contains(n.lowercase())) {
                if (n.length > bestLen) {
                    bestLen = n.length
                    bestCategory = c
                }
            }
        }
        for (p in ragProductRepository.findAll(region)) consider(p.name, p.category)
        for (s in ragServiceRepository.findAll(region)) consider(s.name, s.category)
        return bestCategory
    }
}
