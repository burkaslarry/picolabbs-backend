package com.aicrm.controller

import com.aicrm.service.ReturningCustomerDemoService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 可選：熟客示範寫入（幂等）。首選係 DB 執行 [migrations/005_returning_customer_demo.sql]。
 * Prototype 可用預設 key；正式環境請改 [PICOLABBS_DEMO_SEED_KEY] 或關閉此端點。
 */
@RestController
@RequestMapping("/api/demo")
class DemoSeedController(
    @Value("\${picolabbs.demo.returning-seed-key:}") private val configuredKey: String,
    private val returningCustomerDemoService: ReturningCustomerDemoService
) {

    private val effectiveKey: String
        get() = configuredKey.ifBlank { DEFAULT_RETURNING_SEED_KEY }

    @PostMapping("/seed-returning-customers")
    fun seedReturning(@RequestBody body: Map<String, Any?>): ResponseEntity<Any> {
        val key = body["key"]?.toString()?.trim().orEmpty()
        if (key.isEmpty()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "body.key is required"))
        }
        if (key != effectiveKey) {
            return ResponseEntity.status(403).body(mapOf("error" to "invalid key"))
        }
        val result = returningCustomerDemoService.ensureReturningCustomerRows()
        return ResponseEntity.ok(
            mapOf(
                "ok" to true,
                "inserted" to result.inserted,
                "hint" to "Refresh 熟客／聯絡；應見 4 筆示範、其中兩個電話各為熟客（同聯絡 2 筆）。"
            )
        )
    }

    companion object {
        /** 與前端 prototype 一致；可用 PICOLABBS_DEMO_SEED_KEY 覆寫。 */
        const val DEFAULT_RETURNING_SEED_KEY = "picolabbs-prototype-returning-seed-v1"
    }
}
