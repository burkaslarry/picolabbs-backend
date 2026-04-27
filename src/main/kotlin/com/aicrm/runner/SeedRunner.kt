package com.aicrm.runner

import com.aicrm.domain.Lead
import com.aicrm.repository.AutomationRuleRepository
import com.aicrm.repository.LeadRepository
import com.aicrm.service.AutomationEngineService
import com.aicrm.service.TriageService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.Instant

/** Seeds automation rules and PicoLabb demo leads — never touches `bni_anchor_*`. */
@Component
@Order(1)
class SeedRunner(
    private val ruleRepository: AutomationRuleRepository,
    private val leadRepository: LeadRepository,
    private val triageService: TriageService,
    private val automationEngineService: AutomationEngineService
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments?) {
        if (ruleRepository.count() == 0L) {
            log.info("Seeding default automation rules.")
            ruleRepository.saveAll(automationEngineService.getDefaultRules())
        }
        if (leadRepository.countLeads() > 0L) {
            log.info("Skip sample leads: database already has {} lead(s).", leadRepository.countLeads())
            return
        }
        log.info("Seeding PicoLabb product-aligned demo leads (20).")
        val sampleInquiries = listOf(
            Sample(
                "whatsapp",
                "想問母親節 iRelief 組合銅鑼灣時代廣場店有冇現貨？可唔可以留貨我週末去拎。",
                "+852 9123 1001",
                "Ms Chan",
                "New",
                "picolabbs_wellness"
            ),
            Sample(
                "whatsapp",
                "媽媽膝頭唔舒服，想買 iKnee 俾佢。可唔可以安排中環附近門市試用同教點用？",
                "+852 9123 1002",
                "Lee Pui Yee",
                "Qualified",
                "picolabbs_hardware_pain"
            ),
            Sample(
                "shopline",
                "Shopline 訂單 #SL-88421：The Shield Pro 組合，已付款，選尖沙咀門市自取。請聯絡確認取貨時間。",
                "+852 9123 1003",
                "Mr Lau",
                "Paid/Deposit",
                "picolabbs_hardware_pain"
            ),
            Sample(
                "whatsapp",
                "請問 iWand 6 同 iWand Pro 有咩分別？價錢同保養期想知多啲。",
                "+852 9123 1004",
                "何先生",
                "New",
                "picolabbs_hardware_pain"
            ),
            Sample(
                "web",
                "官網表格：想了解下 PVA 補水機係咪適合乾燥機艙環境用，有冇門市可以睇實物？",
                "ho@example.com",
                "陳太",
                "Needs Info",
                "picolabbs_wellness"
            ),
            Sample(
                "whatsapp",
                "你哋有冇益生菌或維他命系列可以配合居家理療儀器一齊用？想買俾老人家。",
                "+852 9123 1005",
                "王小姐",
                "New",
                "picolabbs_supplements"
            ),
            Sample(
                "whatsapp",
                "狗狗行路無力，想問 PicoLabb 寵物關節配方有冇試用裝或建議劑量？",
                "+852 9123 1006",
                "Karen",
                "New",
                "picolabbs_pet_care"
            ),
            Sample(
                "shopline",
                "Shopline 未付款：母親節 iRelief 禮盒 x1，客人想改去屯門市廣場自取。",
                "+852 9123 1007",
                "周小姐",
                "New",
                "picolabbs_wellness"
            ),
            Sample(
                "whatsapp",
                "屯門店有冇 iWand Pro 現貨？唔想等訂貨，今日傍晚想過去。",
                "+852 9123 1008",
                "張先生",
                "Qualified",
                "picolabbs_hardware_pain"
            ),
            Sample(
                "web",
                "想預約沙田新城市廣場店試 The Shield 家用防護組合，週末下午得唔得？",
                "booking@example.com",
                "Ms Ng",
                "Offered Slots",
                "picolabbs_wellness"
            ),
            Sample(
                "whatsapp",
                "請問鑽石山荷里活廣場店幾點關門？想順路取網上訂嘅 iRelief 配件。",
                "+852 9123 1009",
                "阿明",
                "New",
                "picolabbs_wellness"
            ),
            Sample(
                "shopline",
                "Shopline 已付款：九龍灣德福 iKnee 套裝，客人要求改週六下午自取。",
                "+852 9123 1010",
                "劉先生",
                "Paid/Deposit",
                "picolabbs_hardware_pain"
            ),
            Sample(
                "whatsapp",
                "買咗 iRelief 唔係好識用個熱敷模式，可唔可以約旺角 MOKO 店職員教一次？",
                "+852 9123 1011",
                "李太",
                "Needs Info",
                "picolabbs_hardware_pain"
            ),
            Sample(
                "web",
                "公司想訂一批 PicoLabb 居家護理禮品當員工福袋，想約人講批量同報價。",
                "corp@example.com",
                "HR May",
                "Qualified",
                "picolabbs_wellness"
            ),
            Sample(
                "whatsapp",
                "將軍澳 Popcorn 店今週 iWand 有冇示範？想帶長輩去睇。",
                "+852 9123 1012",
                "Ivy",
                "New",
                "picolabbs_wellness"
            ),
            Sample(
                "whatsapp",
                "荃灣廣場店母親節套裝最遲幾時截單？想買 The Shield Pro 加 iRelief 小套裝。",
                "+852 9123 1013",
                "郭小姐",
                "New",
                "picolabbs_wellness"
            ),
            Sample(
                "shopline",
                "Shopline：客人已選旺角 MOKO 取貨，想改遲一日，訂單編號 SL-99203。",
                "+852 9123 1014",
                "鄭先生",
                "Booked",
                "picolabbs_hardware_pain"
            ),
            Sample(
                "whatsapp",
                "鰂魚涌康怡廣場店聽日有冇 iKnee 即場試用？想確認唔使預約定可以 walk-in。",
                "+852 9123 1015",
                "Eric",
                "Qualified",
                "picolabbs_hardware_pain"
            ),
            Sample(
                "web",
                "想寄一套 iWand 配件去台灣親戚，請問運費同關稅點計？",
                "twship@example.com",
                "Susan",
                "Needs Info",
                "picolabbs_wellness"
            ),
            Sample(
                "whatsapp",
                "膠原肽飲同你哋居家理療系列有冇套裝優惠？想一次過買齊。",
                "+852 9123 1016",
                "趙小姐",
                "New",
                "picolabbs_supplements"
            )
        )
        require(sampleInquiries.size == 20) { "Expected 20 PicoLabb demo leads" }
        // Second inquiry from 何先生 — same contact as earlier sample for built-in 「熟客」 pairs in fresh DBs.
        val inquiries = sampleInquiries.toMutableList()
        inquiries[16] = inquiries[16].copy(
            contact = "+852 9123 1004",
            name = "何先生",
            vertical = "picolabbs_supplements"
        )
        val leadIdBase = 100_001
        val timelineIdBase = 890_000_001
        inquiries.forEachIndexed { index, s ->
            val id = "${leadIdBase + index}"
            val timelineId = "${timelineIdBase + index}"
            val now = Instant.now().minusSeconds((inquiries.size - index) * 90L)
            val lead = Lead(
                id = id,
                channel = s.channel,
                rawMessage = s.raw,
                name = s.name,
                contact = s.contact,
                createdAt = now,
                updatedAt = now,
                stage = s.stage,
                ownerId = null,
                vertical = s.vertical,
                verticalDisplayName = null,
                source = "picolabbs_demo",
                serviceDate = null
            )
            leadRepository.insert(lead)
            leadRepository.insertTimeline(
                timelineId,
                id,
                if (s.channel == "web") "created" else if (s.channel == "shopline") "shopline_sync" else "whatsapp_paste",
                "{}"
            )
            val triageResult = triageService.runTriage(s.raw, id)
            val aiTriage = triageService.toAiTriage(id, triageResult).copy(
                vertical = s.vertical,
                category = s.vertical
            )
            leadRepository.insertOrReplaceTriage(aiTriage)
            leadRepository.updateVertical(id, s.vertical)
            automationEngineService.applyAutomations(id)
        }
        log.info("Seed done: PicoLabb demo — {} sample leads.", inquiries.size)
    }

    private data class Sample(
        val channel: String,
        val raw: String,
        val contact: String?,
        val name: String?,
        val stage: String,
        val vertical: String
    )
}
