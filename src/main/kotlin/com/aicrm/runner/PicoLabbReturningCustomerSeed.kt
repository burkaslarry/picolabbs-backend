package com.aicrm.runner

import com.aicrm.service.ReturningCustomerDemoService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * 開機時寫入／同步熟客示範（兩個電話 × 兩筆查詢）。
 * 若遠端從未成功插入，可再用 [com.aicrm.controller.DemoSeedController] 手動觸發。
 */
@Component
@Order(2)
class PicoLabbReturningCustomerSeed(
    private val returningCustomerDemoService: ReturningCustomerDemoService
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments?) {
        try {
            val r = returningCustomerDemoService.ensureReturningCustomerRows()
            if (r.inserted == 0) {
                log.debug("Returning-customer demo: no new rows (already present or skipped).")
            }
        } catch (e: Exception) {
            log.error("Returning-customer demo seed failed — 熟客示範可能未寫入。可 POST /api/demo/seed-returning-customers 重試。", e)
        }
    }
}
