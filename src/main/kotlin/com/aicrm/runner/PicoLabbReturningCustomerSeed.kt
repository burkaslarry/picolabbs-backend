package com.aicrm.runner

import com.aicrm.domain.Lead
import com.aicrm.repository.LeadRepository
import com.aicrm.service.AutomationEngineService
import com.aicrm.service.TriageService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Idempotent demo rows so 「只顯示熟客」 always has multiple hits in empty or existing DBs.
 * Two phone numbers × two inquiries each (翻買); fixed numeric lead / timeline ids — no UUID.
 */
@Component
@Order(2)
class PicoLabbReturningCustomerSeed(
    private val jdbc: JdbcTemplate,
    private val leadRepository: LeadRepository,
    private val triageService: TriageService,
    private val automationEngineService: AutomationEngineService
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    private data class PairSeed(
        val idFirst: String,
        val idSecond: String,
        val timelineFirst: String,
        val timelineSecond: String,
        val contact: String,
        val name: String,
        val vertical: String,
        val msgFirst: String,
        val msgSecond: String
    )

    override fun run(args: ApplicationArguments?) {
        val pairs = listOf(
            PairSeed(
                idFirst = "88001001",
                idSecond = "88001002",
                timelineFirst = "891000001",
                timelineSecond = "891000002",
                contact = "+852 6111 2222",
                name = "陳小姐",
                vertical = "picolabbs_wellness",
                msgFirst = "想問銅鑼灣時代廣場店母親節 iRelief 禮盒仲有冇現貨？可唔可以留貨週末取。",
                msgSecond = "跟進返上次 iRelief 查詢：想改去尖沙咀門市取貨，請問要點改？"
            ),
            PairSeed(
                idFirst = "88001003",
                idSecond = "88001004",
                timelineFirst = "891000003",
                timelineSecond = "891000004",
                contact = "+852 9999 8888",
                name = "周先生",
                vertical = "picolabbs_hardware_pain",
                msgFirst = "媽媽膝頭痛，想買 iKnee 俾佢，中環店可唔可以試用？",
                msgSecond = "上週問過 iKnee，想加多一部 iWand Pro 一齊寄屋企，有冇套裝價？"
            )
        )

        var inserted = 0
        for (p in pairs) {
            if (!rowExists(p.idFirst)) {
                insertLead(p.idFirst, p.timelineFirst, p.contact, p.name, p.vertical, p.msgFirst, daysAgo = 14)
                inserted++
            }
            if (!rowExists(p.idSecond)) {
                insertLead(p.idSecond, p.timelineSecond, p.contact, p.name, p.vertical, p.msgSecond, daysAgo = 2)
                inserted++
            }
        }
        if (inserted > 0) {
            log.info("PicoLabb returning-customer seed: inserted {} row(s).", inserted)
        }
        syncReturningSeedContactMetadata(pairs)
    }

    /** Keep demo phones / names in sync when ids already existed from an older deploy. */
    private fun syncReturningSeedContactMetadata(pairs: List<PairSeed>) {
        for (p in pairs) {
            for (id in listOf(p.idFirst, p.idSecond)) {
                if (!rowExists(id)) continue
                jdbc.update(
                    """UPDATE aicrm_picolabbs_leads SET contact = ?, name = ?, vertical = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?""",
                    p.contact, p.name, p.vertical, id
                )
            }
        }
    }

    private fun rowExists(id: String): Boolean {
        val n = jdbc.queryForObject(
            "SELECT COUNT(*) FROM aicrm_picolabbs_leads WHERE id = ?",
            Int::class.java,
            id
        ) ?: 0
        return n > 0
    }

    private fun insertLead(
        id: String,
        timelineId: String,
        contact: String,
        name: String,
        vertical: String,
        raw: String,
        daysAgo: Long
    ) {
        val t = Instant.now().minusSeconds(daysAgo * 86400)
        val lead = Lead(
            id = id,
            channel = "whatsapp",
            rawMessage = raw,
            name = name,
            contact = contact,
            createdAt = t,
            updatedAt = t,
            stage = "New",
            ownerId = null,
            vertical = vertical,
            verticalDisplayName = null,
            source = "picolabbs_returning_seed",
            serviceDate = null
        )
        leadRepository.insert(lead)
        leadRepository.insertTimeline(timelineId, id, "whatsapp_paste", "{}")
        val triageResult = triageService.runTriage(raw, id)
        val aiTriage = triageService.toAiTriage(id, triageResult).copy(
            vertical = vertical,
            category = vertical
        )
        leadRepository.insertOrReplaceTriage(aiTriage)
        leadRepository.updateVertical(id, vertical)
        automationEngineService.applyAutomations(id)
    }
}
