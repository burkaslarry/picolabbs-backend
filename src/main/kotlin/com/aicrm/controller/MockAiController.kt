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
        val isZoe = contactName.contains("zoe", true) ||
            listOf("小組", "small group", "pt", "whey", "supplement", "課", "堂", "fitness", "training").any { t.contains(it) }
        val isPicolab = contactName.contains("picolab", true) ||
            listOf("design", "scope", "timeline", "quote", "figma", "stack", "hosting", "maintenance", "invoice").any { t.contains(it) }

        return when {
            isZoe && (t.contains("small group") || t.contains("小組")) -> zoeNewInquiry()
            isZoe && (t.contains("whey") || t.contains("supplement") || t.contains("新貨")) -> zoeSupplement()
            isZoe && (t.contains("改") || t.contains("reschedule") || t.contains("postpone")) -> zoeReschedule()
            isZoe && (t.contains("online") || t.contains("zoom")) -> zoeOnline()
            isZoe && (t.contains("pt") || t.contains("personal")) -> zoePt()
            isZoe && (t.contains("corporate") || t.contains("company") || t.contains("公司")) -> zoeCorporate()
            isZoe && (t.contains("擠") || t.contains("crowded") || t.contains("太多人")) -> zoeComplaint()
            isZoe -> zoeGeneric()

            isPicolab && (t.contains("quote") || t.contains("scope") || t.contains("budget")) -> picoQuote()
            isPicolab && t.contains("timeline") -> picoTimeline()
            isPicolab && (t.contains("stack") || t.contains("tech")) -> picoStack()
            isPicolab && (t.contains("invoice") || t.contains("payment")) -> picoInvoice()
            isPicolab && (t.contains("hosting") || t.contains("maintenance")) -> picoHosting()
            isPicolab && (t.contains("delay") || t.contains("deadline") || t.contains("late")) -> picoDelay()
            isPicolab -> picoGeneric()

            else -> generic()
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

    private fun zoeNewInquiry() = payload(
        "New inquiry", "High", "新客問小組價格，轉化率高。", "問 fitness goal + 可用時段，再 offer free trial。",
        "Hi 多謝你查詢，小組仲有 2 位，每堂 $380，4 堂 $1,400。你想減脂定力量？我可以幫你安排免費試堂。",
        "Thanks for reaching out. We have 2 spots left: $380/class or 4 classes for $1,400. Are you focused on fat loss or strength? Happy to arrange a free trial.",
        "Qualified", "有明確購買意圖，可入 Qualified。", 4, "4 小時未回覆就提醒一次。"
    )
    private fun zoeSupplement() = payload(
        "Existing customer", "Medium", "舊客翻購 supplement。", "確認口味並 upsell shaker。",
        "Whey 新貨到咗，chocolate / vanilla 都有，會員 9 折。你要邊隻？加 shaker 可免運費。",
        "Whey is back in stock (chocolate/vanilla) with member discount. Which one would you like? Add a shaker for free shipping.",
        null, "舊客補貨唔需要移動 pipeline。", 24, "24 小時後再追一次。"
    )
    private fun zoeReschedule() = payload(
        "Existing customer", "Medium", "30 日內多次改期，需要柔性提醒。", "確認新時段並提示 24h 改期政策。",
        "冇問題，幫你改到今晚 9pm。小提醒：之後盡量 24 小時前通知，方便我安排其他學員位置，今晚見！",
        "No problem, moved to 9pm tonight. Friendly reminder to notify 24h ahead next time so I can manage other slots. See you tonight!",
        null, "維持現 stage，標記 retention attention。", 2, "2 小時前自動提醒。"
    )
    private fun zoeOnline() = payload(
        "New inquiry", "High", "遠程學員有高轉化潛力。", "介紹 online 方案並收集 timezone。",
        "有 online！1-on-1 $480，online 小組 $280。你係邊個 timezone？每週想幾多堂？",
        "Yes, we offer online plans: 1-on-1 at $480, group at $280. What's your timezone and preferred weekly frequency?",
        "Qualified", "高意圖新客。", 6, "6 小時後提醒。"
    )
    private fun zoePt() = payload(
        "Existing customer", "High", "舊客升級 PT，客單價高。", "報 3/5/10 堂 package。",
        "Perfect，PT $680/堂；5 堂 $3,200；10 堂 $6,000。你下週邊日方便？我幫你鎖位。",
        "Perfect. PT is $680/session, 5-pack $3,200, 10-pack $6,000. Which days next week work for you?",
        "Proposal", "進入報價階段。", 8, "8 小時未回覆就 follow up。"
    )
    private fun zoeCorporate() = payload(
        "New inquiry", "High", "企業 wellness 屬高價值 B2B。", "約 15 分鐘 discovery call。",
        "多謝查詢！Corporate wellness 有月 4 堂 $12k、月 8 堂 $22k。可以約 15 分鐘了解公司規模嗎？",
        "Thanks for your interest. Corporate wellness packages start at $12k/month. Can we schedule a 15-min discovery call?",
        "Qualified", "高價值線索需優先跟進。", 2, "2 小時內再次提醒。"
    )
    private fun zoeComplaint() = payload(
        "Complaint", "High", "體驗投訴需即時處理。", "道歉 + 補救行動 + 補堂方案。",
        "真係唔好意思，今堂安排得唔夠好。我會預留固定位置，另外補返一堂 PT 畀你。你想邊日補？",
        "Sincere apologies for today’s experience. I’ll reserve a fixed slot and offer a complimentary PT session. Which day works for your makeup?",
        null, "保留 stage，標記 retention risk。", 1, "1 小時內跟進。"
    )
    private fun zoeGeneric() = payload(
        "Existing customer", "Medium", "一般對話。", "友善回覆並了解需求。",
        "收到你 message，多謝你！我想了解多啲你目標，先幫你安排最啱嘅方案。",
        "Thanks for your message. Let me understand your goals first and I’ll suggest the best-fit plan.",
        null, "N/A", 24, "24 小時 check-in。"
    )

    private fun picoQuote() = payload(
        "Quotation", "High", "新 B2B quote 入線。", "先做 3 條 qualifying question。",
        "多謝查詢！我哋項目通常 $80k-$250k，想先了解目標客群、deadline 同 budget，方便安排 30 分鐘 call。",
        "Thanks for reaching out. Our projects are typically $80k-$250k. Could you share target users, deadline, and budget before we schedule a 30-min call?",
        "Qualified", "新客 + quote 意向。", 24, "24 小時 gentle nudge。"
    )
    private fun picoTimeline() = payload(
        "Existing customer", "High", "現有 project 進度詢問。", "提供 milestone ETA + sync call。",
        "Quick update：design system 同 homepage 已完成，今週五交 Figma。下個 milestone 係 product pages（4/29）。",
        "Quick update: design system and homepage are complete, Figma will be shared this Friday. Next milestone is product pages (Apr 29).",
        null, "保持現 stage。", 48, "48 小時內確認交付。"
    )
    private fun picoStack() = payload(
        "New inquiry", "Medium", "技術盡調，意向較高。", "說明 stack 並轉 architecture call。",
        "我哋通常用 Next.js + Tailwind + headless CMS。重點係你 content flow，想唔想約 30 分鐘講 architecture？",
        "Our default stack is Next.js + Tailwind + headless CMS. The key is your content flow—want a 30-min architecture call?",
        "Qualified", "技術問題通常代表認真考慮。", 24, "24 小時約 call。"
    )
    private fun picoInvoice() = payload(
        "Existing customer", "High", "涉及付款，需即時回覆。", "重發 invoice 並確認收件。",
        "Invoice 已重發到你 email（NET 14），如果你想 FPS 付款我可以即刻補資料。",
        "Invoice has been resent (NET 14). If you prefer FPS transfer, I can share details right away.",
        null, "N/A", 4, "4 小時內確認對方已收。"
    )
    private fun picoHosting() = payload(
        "New inquiry", "Medium", "維護方案屬 recurring revenue。", "提供 3-tier maintenance plan。",
        "有 3 個 maintenance plan（Basic/Growth/Pro），可按你流量同開發需求建議最合適 tier。",
        "We offer 3 maintenance tiers (Basic/Growth/Pro). I can recommend the best fit based on your traffic and dev needs.",
        "Qualified", "可轉 recurring deal。", 24, "24 小時提供 comparison doc。"
    )
    private fun picoDelay() = payload(
        "Complaint", "High", "延期風險傷害信任。", "承認 + 新 ETA + 補償方案。",
        "真係抱歉，我應該提早講。新 ETA 係本週五，另外我會免費加 3 個 landing page variants 畀你做 A/B test。",
        "I’m sorry I should have flagged this earlier. New ETA is this Friday, and I’ll add 3 landing page variants for A/B testing at no extra cost.",
        null, "保 stage，標記 retention risk。", 1, "1 小時內確認新計劃。"
    )
    private fun picoGeneric() = payload(
        "Existing customer", "Medium", "一般 project 對話。", "快速回覆並提出下一步。",
        "收到，thanks！我整理好細節後即刻回覆你。",
        "Noted, thanks! I’ll get back to you shortly with details.",
        null, "N/A", 24, "24h check-in。"
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
