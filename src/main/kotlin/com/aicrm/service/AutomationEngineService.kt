package com.aicrm.service

import com.aicrm.domain.AutomationRule
import com.aicrm.repository.AutomationRuleRepository
import com.aicrm.repository.LeadRepository
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.atomic.AtomicLong
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ApplyResult(val applied: List<AppliedAction>, val newStage: String?, val newOwner: String?)

data class AppliedAction(val rule: String, val action: String, val value: String? = null, val title: String? = null)

@Service
class AutomationEngineService(
    private val leadRepository: LeadRepository,
    private val ruleRepository: AutomationRuleRepository,
    private val objectMapper: ObjectMapper
) {

    /** Monotonic numeric string ids for automation-created tasks / timeline (no UUID). */
    private val automationIdSeq = AtomicLong(9_520_000_001L)

    private fun nextAutomationEntityId(): String = automationIdSeq.getAndIncrement().toString()

    fun getDefaultRules(): List<AutomationRule> = listOf(
        AutomationRule("rule-whatsapp-inquiry", "【前期】WhatsApp 產品查詢自動派單",
            """{"channel":"whatsapp","stage":"New"}""",
            """[{"type":"assign_owner","value":"90000000-0000-4000-8000-000000000102"},{"type":"create_task","taskType":"reply","title":"Draft WhatsApp reply for product inquiry","dueMinutes":30}]""",
            1, 10),
        AutomationRule("rule-shopline-paid", "【後期】Shopline 已付款訂單安排門市自取",
            """{"channel":"shopline","stage":"Paid/Deposit"}""",
            """[{"type":"assign_owner","value":"90000000-0000-4000-8000-000000000103"},{"type":"create_task","taskType":"booking","title":"聯絡客人安排門市自取時間","dueMinutes":60}]""",
            1, 20),
        AutomationRule("rule-missing-info", "缺少分店偏好",
            """{"missingFieldsContains":"branch_preference"}""",
            """[{"type":"set_stage","value":"Needs Info"},{"type":"draft_message","askTopMissing":1}]""",
            1, 30)
    )

    fun applyAutomations(leadId: String): ApplyResult {
        val lead = leadRepository.findById(leadId) ?: return ApplyResult(emptyList(), null, null)
        val triage = leadRepository.getTriage(leadId) ?: return ApplyResult(emptyList(), null, null)

        @Suppress("UNCHECKED_CAST")
        val missing = (objectMapper.readValue<List<*>>(triage.missingFields ?: "[]")).map { it.toString() }
        val vertical = triage.vertical ?: lead.vertical ?: "unknown"
        val intent = triage.intent ?: "info"
        val urgency = triage.urgencyScore ?: 0
        val safetyEscalate = triage.safetyEscalate == 1

        val rules = ruleRepository.findAllByEnabledOrderBySortOrder()
        val applied = mutableListOf<AppliedAction>()
        var newStage: String? = lead.stage
        var newOwner: String? = lead.ownerId

        for (rule in rules) {
            val cond = objectMapper.readValue<Map<String, Any?>>(rule.triggerCondition)
            var match = false
            if (cond["intent"] == intent && cond["hasTriage"] == true) match = true
            if (cond["missingFieldsNotEmpty"] == true && missing.isNotEmpty()) match = true
            if (cond["urgencyMin"] != null && urgency >= (cond["urgencyMin"] as Number).toInt()) match = true
            if (cond["intent"] == "complaint" && cond["safetyEscalate"] == true && intent == "complaint" && safetyEscalate) match = true

            if (!match) continue

            @Suppress("UNCHECKED_CAST")
            val actions = objectMapper.readValue<List<Map<String, Any?>>>(rule.actions)
            for (action in actions) {
                when (action["type"]) {
                    "set_stage" -> {
                        val value = action["value"]?.toString() ?: continue
                        newStage = value
                        leadRepository.updateStage(leadId, value)
                        applied.add(AppliedAction(rule.name, "set_stage", value))
                    }
                    "assign_owner" -> {
                        val valueByVertical = action["valueByVertical"] as? Map<*, *>
                        var owner = (valueByVertical?.get(vertical) ?: action["value"])?.toString()
                        if (owner == null && vertical.startsWith("picolabbs_")) {
                            owner = "90000000-0000-4000-8000-000000000102"
                        }
                        if (owner != null) {
                            newOwner = owner
                            leadRepository.updateOwner(leadId, owner)
                            applied.add(AppliedAction(rule.name, "assign_owner", owner))
                        }
                    }
                    "create_task" -> {
                        val taskType = action["taskType"]?.toString() ?: "general"
                        val title = action["title"]?.toString() ?: "Task"
                        val dueMin = (action["dueMinutes"] as? Number)?.toInt() ?: 15
                        val dueAt = LocalDateTime.now().plusMinutes(dueMin.toLong()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        leadRepository.insertTask(nextAutomationEntityId(), leadId, taskType, title, dueAt)
                        applied.add(AppliedAction(rule.name, "create_task", title = title))
                    }
                }
            }
        }

        leadRepository.insertTimeline(
            nextAutomationEntityId(),
            leadId,
            "automations_applied",
            objectMapper.writeValueAsString(mapOf("applied" to applied))
        )
        return ApplyResult(applied, newStage, newOwner)
    }
}
