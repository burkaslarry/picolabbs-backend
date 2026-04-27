package com.aicrm.controller

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/ai")
@RestController
@CrossOrigin(origins = ["*"])
class MockAiController {

    @PostMapping("/analyze")
    fun analyze(@RequestBody req: AnalyzeRequest): Map<String, Any?> {
        Thread.sleep(800 + (Math.random() * 600).toLong())
        return matchFixture(req.messageText, req.contactName)
    }

    private fun matchFixture(text: String, contactName: String): Map<String, Any?> {
        val t = text.lowercase()
        val isPico =
            contactName.contains("pico", true) ||
                listOf(
                    "iwand", "irelief", "shield", "iknee", "picolabb", "門市", "分店", "取貨",
                    "shopline", "母親節", "理療", "試用", "寵物", "狗狗", "膠原", "保健"
                ).any { t.contains(it) } ||
                listOf(
                    "寵物", "門市", "取貨", "訂單", "iwand", "irelief", "母親節", "理療", "膠原"
                ).any { text.contains(it) }

        return when {
            !isPico -> generic()

            (t.contains("shopline") || t.contains("訂單") || t.contains("取貨")) -> picoPickup()
            (t.contains("寵物") || t.contains("狗") || t.contains("貓") || t.contains("happy pet")) -> picoPet()
            (t.contains("iwand") && (t.contains("分別") || t.contains("vs") || t.contains("pro"))) -> picoCompare()
            (t.contains("投訴") || t.contains("壞") || t.contains("complaint") || t.contains("無效")) -> picoComplaint()
            (t.contains("膠原") || t.contains("supplement") || t.contains("保健") || t.contains("維他命")) -> picoSupplements()
            (t.contains("價") || t.contains("price") || t.contains("幾錢")) -> picoPrice()
            (t.contains("預約") || t.contains("book") || t.contains("試用")) -> picoBook()
            else -> picoGeneric()
        }
    }

    private fun payload(
        category: String,
        priority: String,
        reasoning: String,
        nextStep: String,
        draftCanto: String,
        draftEnglish: String,
        suggestedStage: String?,
        stageReasoning: String,
        dueInHours: Int,
        note: String
    ) = mapOf(
        "triage" to mapOf("category" to category, "priority" to priority, "reasoning" to reasoning),
        "next_step" to nextStep,
        "draft_canto" to draftCanto,
        "draft_english" to draftEnglish,
        "pipeline_suggestion" to mapOf("suggested_stage" to suggestedStage, "reasoning" to stageReasoning),
        "follow_up" to mapOf("due_in_hours" to dueInHours, "note" to note)
    )

    private fun picoPickup() = payload(
        "Order / pickup", "High", "已付款或門市取貨相關，需盡快確認時間。", "核對訂單編號、分店及可取貨時段。",
        "收到，我幫你核對 Shopline 訂單同門市庫存，稍後 WhatsApp 回覆你可取貨時間。",
        "Thanks — I’ll verify your Shopline order and branch stock, then reply with pickup options.",
        "Qualified", "有明確交易編號，可優先處理。", 4, "4 小時內回覆取貨安排。"
    )

    private fun picoPet() = payload(
        "Pet care", "Medium", "寵物關節／營養查詢，建議轉門市或產品同事。", "確認寵物體重、年齡及想改善嘅情況。",
        "多謝查詢 PicoLabb 寵物系列。想了解下毛孩大約幾多歲、體重同而家行路情況，方便建議合適配方。",
        "Thanks for asking about our pet line. Could you share your pet’s age, weight, and mobility so we can suggest a suitable formula?",
        "Needs Info", "需補充寵物資料。", 24, "24 小時內 gentle follow-up。"
    )

    private fun picoCompare() = payload(
        "Product comparison", "Medium", "型號比較屬高意圖售前問題。", "用三點講清功能、保養與門市試用。",
        "iWand 6 同 Pro 主要係強度模式同配件組合唔同；如果你想，我可以安排門市試用同教點用。",
        "iWand 6 vs Pro mainly differ in modes and bundled accessories. I can arrange an in-store demo if helpful.",
        "Qualified", "比較型號通常接近決策。", 12, "12 小時內跟進門市時段。"
    )

    private fun picoComplaint() = payload(
        "Complaint", "High", "產品體驗問題需即時安撫同記錄。", "道歉、記錄批次／購買渠道、安排門市跟進。",
        "真係唔好意思令你困擾。我想了解返購買日期同使用情況，會安排同事盡快同你跟進。",
        "I’m sorry for the trouble. Please share purchase date and usage details so we can follow up quickly.",
        null, "保留 stage，標記需主管留意。", 2, "2 小時內首次回覆。"
    )

    private fun picoSupplements() = payload(
        "Supplements", "Medium", "保健品諮詢可連帶儀器套裝。", "確認服用習慣同是否有長期病患／藥物。",
        "我哋有膠原肽飲同維他命系列，可配合居家理療一齊用。想問下有冇長期病患或正在服用藥物？",
        "We have collagen drinks and vitamins that pair well with home devices. Any chronic conditions or medications we should know about?",
        "Needs Info", "需安全相關資料。", 24, "24 小時內再跟進。"
    )

    private fun picoPrice() = payload(
        "Pricing", "High", "價格查詢建議盡快給清晰區間。", "說明套裝／單品大致範圍並邀請到門市確認。",
        "價錢會視乎套裝同門市推廣略有唔同，我整理最新報價表同優惠，稍後發你。",
        "Pricing varies slightly by bundle and in-store promos — I’ll send the latest options shortly.",
        "Qualified", "有購買意圖。", 6, "6 小時內發報價重點。"
    )

    private fun picoBook() = payload(
        "Booking", "High", "試用／預約應鎖定分店與時段。", "提供 2–3 個門市時段選擇。",
        "收到，想約邊區門市試用？我可以幫你睇今週下午／週末邊啲時段有位。",
        "Got it — which branch works for you? I can suggest 2–3 slots this week.",
        "Offered Slots", "可進入排程。", 8, "8 小時內確認時段。"
    )

    private fun picoGeneric() = payload(
        "Product inquiry", "Medium", "一般 PicoLabb 產品查詢。", "了解產品、分店偏好同聯絡方式。",
        "多謝聯絡 PicoLabb！想問你主要關心邊類產品（理療儀器／保健／寵物），同邊區門市最方便？",
        "Thanks for contacting PicoLabb — which product line are you interested in (devices, wellness, pet), and which area is easiest for you?",
        "New", "新查詢。", 24, "24 小時內回覆。"
    )

    private fun generic() = payload(
        "Other", "Low", "未能明確分類。", "建議人手 review。",
        "收到你嘅訊息，我會先幫你整理，稍後回覆你。",
        "Got your message, I’ll review and reply shortly.",
        null, "N/A", 24, "24h check-in。"
    )
}

data class AnalyzeRequest(
    val messageText: String,
    val contactName: String = "",
    val phone: String = ""
)
