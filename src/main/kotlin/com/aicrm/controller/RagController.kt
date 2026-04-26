package com.aicrm.controller

import com.aicrm.domain.RagProduct
import com.aicrm.domain.RagService
import com.aicrm.domain.FollowUpCase
import com.aicrm.domain.Lead
import com.aicrm.domain.RagDocument
import com.aicrm.domain.RagDocumentLink
import com.aicrm.domain.validateChannel
import com.aicrm.domain.validateISODate
import com.aicrm.domain.validateStage
import com.aicrm.repository.FollowUpCaseRepository
import com.aicrm.repository.LeadRepository
import com.aicrm.repository.RagDocumentRepository
import com.aicrm.repository.RagProductRepository
import com.aicrm.repository.RagServiceRepository
import com.aicrm.util.uuid
import org.springframework.http.ResponseEntity
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

@RequestMapping("/api/rag")
@RestController
class RagController(
    private val ragServiceRepository: RagServiceRepository,
    private val ragProductRepository: RagProductRepository,
    private val leadRepository: LeadRepository,
    private val followUpCaseRepository: FollowUpCaseRepository,
    private val ragDocumentRepository: RagDocumentRepository
) {

    @GetMapping("/services")
    fun listServices(@RequestParam(required = false) region: String?): List<Map<String, Any?>> =
        ragServiceRepository.findAll(region).map { toServiceMap(it) }

    @GetMapping("/products")
    fun listProducts(@RequestParam(required = false) region: String?): List<Map<String, Any?>> =
        ragProductRepository.findAll(region).map { toProductMap(it) }

    @GetMapping("/services/search")
    fun searchServices(
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) q: String?
    ): List<Map<String, Any?>> =
        ragServiceRepository.searchByRegionAndKeyword(region, q).map { toServiceMap(it) }

    @GetMapping("/products/search")
    fun searchProducts(
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) q: String?
    ): List<Map<String, Any?>> =
        ragProductRepository.searchByRegionAndKeyword(region, q).map { toProductMap(it) }

    @GetMapping("/imported-leads")
    fun listImportedLeads(): List<Map<String, Any?>> =
        leadRepository.findAll().take(300).map { toLeadMap(it) }

    @GetMapping("/follow-up-cases")
    fun listFollowUpCases(): List<Map<String, Any?>> =
        followUpCaseRepository.findAll().take(300).map { toCaseMap(it) }

    @PostMapping("/services/import")
    fun importServicesCsv(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        if (file.isEmpty) return ResponseEntity.badRequest().body(mapOf("error" to "Empty file"))
        val rows = parseCsv(file.bytes)
        if (rows.isEmpty()) return ResponseEntity.badRequest().body(mapOf("error" to "No rows in CSV"))
        val header = rows[0].map { it.lowercase().trim() }
        val nameIdx = header.indexOfFirst { it == "name" || it == "名稱" }
        val descIdx = header.indexOfFirst { it == "description" || it == "desc" || it == "描述" }
        val regionIdx = header.indexOfFirst { it == "region" || it == "地區" }
        val categoryIdx = header.indexOfFirst { it == "category" || it == "大類" || it == "分類" }
        if (nameIdx < 0) return ResponseEntity.badRequest().body(mapOf("error" to "CSV must have a 'name' column"))
        var imported = 0
        for (i in 1 until rows.size) {
            val row = rows[i]
            val nameVal = row.getOrNull(nameIdx)?.trim()
            if (nameVal.isNullOrBlank()) continue
            val name = nameVal
            val description = if (descIdx >= 0 && descIdx < row.size) row[descIdx].trim().take(5000) else null
            val category = if (categoryIdx >= 0 && categoryIdx < row.size) row[categoryIdx].trim().take(255) else null
            val region = when {
                regionIdx >= 0 && regionIdx < row.size -> normalizeRegion(row[regionIdx].trim())
                else -> "hk"
            }
            ragServiceRepository.insert(
                RagService(
                    id = uuid(),
                    name = name.take(500),
                    description = description?.ifBlank { null },
                    region = region,
                    category = category?.ifBlank { null },
                    createdAt = Instant.now()
                )
            )
            imported++
        }
        return ResponseEntity.ok(mapOf("imported" to imported, "total" to rows.size - 1))
    }

    @PostMapping("/products/import")
    fun importProductsCsv(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        if (file.isEmpty) return ResponseEntity.badRequest().body(mapOf("error" to "Empty file"))
        val rows = parseCsv(file.bytes)
        if (rows.isEmpty()) return ResponseEntity.badRequest().body(mapOf("error" to "No rows in CSV"))
        val header = rows[0].map { it.lowercase().trim() }
        val nameIdx = header.indexOfFirst { it == "name" || it == "名稱" }
        val descIdx = header.indexOfFirst { it == "description" || it == "desc" || it == "描述" }
        val regionIdx = header.indexOfFirst { it == "region" || it == "地區" }
        val categoryIdx = header.indexOfFirst { it == "category" || it == "大類" || it == "分類" }
        if (nameIdx < 0) return ResponseEntity.badRequest().body(mapOf("error" to "CSV must have a 'name' column"))
        var imported = 0
        for (i in 1 until rows.size) {
            val row = rows[i]
            val nameVal = row.getOrNull(nameIdx)?.trim()
            if (nameVal.isNullOrBlank()) continue
            val name = nameVal
            val description = if (descIdx >= 0 && descIdx < row.size) row[descIdx].trim().take(5000) else null
            val category = if (categoryIdx >= 0 && categoryIdx < row.size) row[categoryIdx].trim().take(255) else null
            val region = when {
                regionIdx >= 0 && regionIdx < row.size -> normalizeRegion(row[regionIdx].trim())
                else -> "hk"
            }
            ragProductRepository.insert(
                RagProduct(id = uuid(), name = name.take(500), description = description?.ifBlank { null }, region = region, category = category?.ifBlank { null }, createdAt = Instant.now())
            )
            imported++
        }
        return ResponseEntity.ok(mapOf("imported" to imported, "total" to rows.size - 1))
    }

    @PostMapping("/import/leads")
    fun importLeadsCsv(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        if (file.isEmpty) return ResponseEntity.badRequest().body(mapOf("error" to "Empty file"))
        val rows = parseCsv(file.bytes)
        if (rows.isEmpty()) return ResponseEntity.badRequest().body(mapOf("error" to "No rows in CSV"))
        val header = rows[0].map { it.lowercase().trim() }
        val rawIdx = header.indexOfFirst { it == "raw_message" || it == "message" || it == "訊息" }
        val channelIdx = header.indexOfFirst { it == "channel" || it == "管道" }
        val nameIdx = header.indexOfFirst { it == "name" || it == "姓名" || it == "名稱" }
        val contactIdx = header.indexOfFirst { it == "contact" || it == "聯絡" }
        val verticalIdx = header.indexOfFirst { it == "vertical" || it == "類別" }
        val sourceIdx = header.indexOfFirst { it == "source" || it == "來源" }
        val stageIdx = header.indexOfFirst { it == "stage" || it == "階段" }
        val serviceDateIdx = header.indexOfFirst { it == "service_date" || it == "服務日期" }
        if (rawIdx < 0) return ResponseEntity.badRequest().body(mapOf("error" to "CSV must have a 'raw_message' column"))

        var imported = 0
        for (i in 1 until rows.size) {
            val row = rows[i]
            val raw = row.getOrNull(rawIdx)?.trim().orEmpty()
            if (raw.isBlank()) continue
            val now = Instant.now()
            val lead = Lead(
                id = uuid(),
                channel = validateChannel(row.getOrNull(channelIdx)?.trim()) ?: "web",
                rawMessage = raw.take(10000),
                name = row.getOrNull(nameIdx)?.trim()?.ifBlank { null }?.take(500),
                contact = row.getOrNull(contactIdx)?.trim()?.ifBlank { null }?.take(500),
                createdAt = now,
                updatedAt = now,
                stage = validateStage(row.getOrNull(stageIdx)?.trim()) ?: "New",
                ownerId = null,
                vertical = row.getOrNull(verticalIdx)?.trim()?.ifBlank { null }?.take(50),
                source = row.getOrNull(sourceIdx)?.trim()?.ifBlank { null }?.take(255),
                serviceDate = validateISODate(row.getOrNull(serviceDateIdx)?.trim())
            )
            leadRepository.insert(lead)
            leadRepository.insertTimeline(uuid(), lead.id, "import_csv_lead", """{"source":"csv"}""")
            imported++
        }
        return ResponseEntity.ok(mapOf("imported" to imported, "total" to rows.size - 1))
    }

    @PostMapping("/import/cases")
    fun importCasesCsv(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> {
        if (file.isEmpty) return ResponseEntity.badRequest().body(mapOf("error" to "Empty file"))
        val rows = parseCsv(file.bytes)
        if (rows.isEmpty()) return ResponseEntity.badRequest().body(mapOf("error" to "No rows in CSV"))
        val header = rows[0].map { it.lowercase().trim() }
        val caseNameIdx = header.indexOfFirst { it == "case_name" || it == "name" || it == "案例名稱" }
        val contactIdx = header.indexOfFirst { it == "contact" || it == "聯絡" }
        val statusIdx = header.indexOfFirst { it == "status" || it == "狀態" }
        val notesIdx = header.indexOfFirst { it == "notes" || it == "備註" }
        val leadRefIdx = header.indexOfFirst { it == "lead_ref" || it == "lead_id" || it == "名單參考" }
        if (caseNameIdx < 0) return ResponseEntity.badRequest().body(mapOf("error" to "CSV must have a 'case_name' column"))

        var imported = 0
        for (i in 1 until rows.size) {
            val row = rows[i]
            val caseName = row.getOrNull(caseNameIdx)?.trim().orEmpty()
            if (caseName.isBlank()) continue
            followUpCaseRepository.insert(
                FollowUpCase(
                    id = uuid(),
                    caseName = caseName.take(500),
                    contact = row.getOrNull(contactIdx)?.trim()?.ifBlank { null }?.take(500),
                    status = row.getOrNull(statusIdx)?.trim()?.ifBlank { null }?.take(50),
                    notes = row.getOrNull(notesIdx)?.trim()?.ifBlank { null }?.take(5000),
                    leadRef = row.getOrNull(leadRefIdx)?.trim()?.ifBlank { null }?.take(64),
                    createdAt = Instant.now()
                )
            )
            imported++
        }
        return ResponseEntity.ok(mapOf("imported" to imported, "total" to rows.size - 1))
    }

    @PostMapping("/documents/import-pdf")
    fun importPdfAndLink(
        @RequestParam("file") file: MultipartFile,
        @RequestParam(required = false) region: String?
    ): ResponseEntity<Any> {
        if (file.isEmpty) return ResponseEntity.badRequest().body(mapOf("error" to "Empty file"))
        val normalizedRegion = normalizeRegion(region ?: "sg")

        val text = try {
            Loader.loadPDF(file.bytes).use { doc ->
                PDFTextStripper().getText(doc).orEmpty()
            }
        } catch (_: Exception) {
            ""
        }
        if (text.isBlank()) return ResponseEntity.badRequest().body(mapOf("error" to "PDF text extraction failed"))

        val document = RagDocument(
            id = uuid(),
            fileName = file.originalFilename?.take(255) ?: "document.pdf",
            region = normalizedRegion,
            extractedText = text.take(200000),
            createdAt = Instant.now()
        )
        ragDocumentRepository.insertDocument(document)

        val allServices = ragServiceRepository.findAll(normalizedRegion)
        val allProducts = ragProductRepository.findAll(normalizedRegion)
        val searchSpace = normalizeForMatch(text)
        var serviceCount = 0
        var productCount = 0

        for (s in allServices) {
            val score = scoreMatch(searchSpace, s.name)
            if (score > 0.0) {
                ragDocumentRepository.insertLink(
                    RagDocumentLink(uuid(), document.id, "service", s.id, s.name, score, Instant.now())
                )
                serviceCount++
            }
        }
        for (p in allProducts) {
            val score = scoreMatch(searchSpace, p.name)
            if (score > 0.0) {
                ragDocumentRepository.insertLink(
                    RagDocumentLink(uuid(), document.id, "product", p.id, p.name, score, Instant.now())
                )
                productCount++
            }
        }
        val links = ragDocumentRepository.getLinksByDocument(document.id)
        return ResponseEntity.ok(
            mapOf(
                "document_id" to document.id,
                "file_name" to document.fileName,
                "region" to document.region,
                "service_links" to serviceCount,
                "product_links" to productCount,
                "matched_count" to links.size,
                "matches" to links.map {
                    mapOf(
                        "item_type" to it.itemType,
                        "item_id" to it.itemId,
                        "item_name" to it.itemName,
                        "score" to it.score
                    )
                }
            )
        )
    }

    @DeleteMapping("/services")
    fun clearServices(): ResponseEntity<Any> {
        ragServiceRepository.deleteAll()
        return ResponseEntity.ok(mapOf("ok" to true))
    }

    @DeleteMapping("/products")
    fun clearProducts(): ResponseEntity<Any> {
        ragProductRepository.deleteAll()
        return ResponseEntity.ok(mapOf("ok" to true))
    }

    private fun toServiceMap(s: RagService) = mapOf(
        "id" to s.id,
        "name" to s.name,
        "description" to s.description,
        "region" to s.region,
        "category" to s.category,
        "created_at" to s.createdAt.toString()
    )

    private fun toProductMap(p: RagProduct) = mapOf(
        "id" to p.id,
        "name" to p.name,
        "description" to p.description,
        "region" to p.region,
        "category" to p.category,
        "created_at" to p.createdAt.toString()
    )

    private fun toLeadMap(lead: Lead) = mapOf(
        "id" to lead.id,
        "channel" to lead.channel,
        "raw_message" to lead.rawMessage,
        "name" to lead.name,
        "contact" to lead.contact,
        "stage" to lead.stage,
        "vertical" to lead.vertical,
        "source" to lead.source,
        "service_date" to lead.serviceDate,
        "created_at" to lead.createdAt.toString()
    )

    private fun toCaseMap(c: FollowUpCase) = mapOf(
        "id" to c.id,
        "case_name" to c.caseName,
        "contact" to c.contact,
        "status" to c.status,
        "notes" to c.notes,
        "lead_ref" to c.leadRef,
        "created_at" to c.createdAt.toString()
    )

    private fun parseCsv(bytes: ByteArray): List<List<String>> {
        val text = bytes.toString(Charsets.UTF_8)
        val lines = text.lines().filter { it.isNotBlank() }
        return lines.map { line ->
            val result = mutableListOf<String>()
            var i = 0
            val sb = StringBuilder()
            while (i < line.length) {
                when (line[i]) {
                    '"' -> {
                        i++
                        while (i < line.length) {
                            if (line[i] == '"') {
                                i++
                                if (i < line.length && line[i] == '"') { sb.append('"'); i++ }
                                else break
                            } else { sb.append(line[i]); i++ }
                        }
                        result.add(sb.toString()); sb.clear()
                    }
                    ',' -> { result.add(sb.toString()); sb.clear(); i++ }
                    else -> { sb.append(line[i]); i++ }
                }
            }
            result.add(sb.toString())
            result
        }
    }

    private fun normalizeRegion(r: String): String =
        when (r.lowercase().trim()) {
            "hk", "hong kong", "香港" -> "hk"
            "tw", "taiwan", "台灣", "台湾" -> "tw"
            "cn", "china", "中國", "中国" -> "cn"
            "sg", "singapore", "新加坡" -> "sg"
            else -> if (r.isNotBlank()) r.take(10) else "hk"
        }

    private fun normalizeForMatch(input: String): String =
        input.lowercase().replace(Regex("[^\\p{L}\\p{N}\\s]"), " ").replace(Regex("\\s+"), " ").trim()

    @PostMapping("/products")
    fun createProduct(@RequestBody body: Map<String, Any?>): ResponseEntity<Any> {
        val name = body["name"]?.toString()?.trim() ?: return ResponseEntity.badRequest().body(mapOf("error" to "Missing name"))
        val description = body["description"]?.toString()?.trim()?.ifBlank { null }
        val region = normalizeRegion(body["region"]?.toString() ?: "hk")
        val category = body["category"]?.toString()?.trim()?.ifBlank { null }
        val product = RagProduct(id = uuid(), name = name.take(500), description = description?.take(5000), region = region, category = category?.take(255), createdAt = Instant.now())
        ragProductRepository.insert(product)
        return ResponseEntity.ok(toProductMap(product))
    }

    @PutMapping("/products/{id}")
    fun updateProduct(@PathVariable id: String, @RequestBody body: Map<String, Any?>): ResponseEntity<Any> {
        val name = body["name"]?.toString()?.trim() ?: return ResponseEntity.badRequest().body(mapOf("error" to "Missing name"))
        val description = body["description"]?.toString()?.trim()?.ifBlank { null }
        val region = normalizeRegion(body["region"]?.toString() ?: "hk")
        val category = body["category"]?.toString()?.trim()?.ifBlank { null }
        ragProductRepository.update(id, name.take(500), description?.take(5000), region, category?.take(255))
        return ResponseEntity.ok(mapOf("ok" to true))
    }

    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: String): ResponseEntity<Any> {
        ragProductRepository.deleteById(id)
        return ResponseEntity.ok(mapOf("ok" to true))
    }

    @PutMapping("/categories")
    fun updateCategory(@RequestBody body: Map<String, Any?>): ResponseEntity<Any> {
        val oldName = body["oldName"]?.toString()?.trim() ?: return ResponseEntity.badRequest().body(mapOf("error" to "Missing oldName"))
        val newName = body["newName"]?.toString()?.trim() ?: return ResponseEntity.badRequest().body(mapOf("error" to "Missing newName"))
        ragProductRepository.updateCategoryName(oldName, newName)
        ragServiceRepository.updateCategoryName(oldName, newName)
        return ResponseEntity.ok(mapOf("ok" to true))
    }

    @DeleteMapping("/categories/{name}")
    fun deleteCategory(@PathVariable name: String): ResponseEntity<Any> {
        ragProductRepository.deleteCategory(name)
        ragServiceRepository.deleteByCategory(name)
        return ResponseEntity.ok(mapOf("ok" to true))
    }

    @GetMapping("/products/export")
    fun exportProductsCsv(): ResponseEntity<ByteArray> {
        val products = ragProductRepository.findAll()
        val sb = StringBuilder()
        sb.append("category,name,description,region\n")
        products.forEach { p ->
            val c = escapeCsv(p.category ?: "")
            val n = escapeCsv(p.name)
            val d = escapeCsv(p.description ?: "")
            val r = escapeCsv(p.region)
            sb.append("${c},${n},${d},${r}\n")
        }
        val bytes = sb.toString().toByteArray(Charsets.UTF_8)
        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType("text/csv")
        headers.setContentDispositionFormData("attachment", "picolabbs_products.csv")
        return ResponseEntity(bytes, headers, org.springframework.http.HttpStatus.OK)
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }

    private fun scoreMatch(documentText: String, itemName: String): Double {
        val n = normalizeForMatch(itemName)
        if (n.isBlank()) return 0.0
        if (documentText.contains(n)) return 1.0
        val tokens = n.split(" ").filter { it.length >= 2 }
        if (tokens.isEmpty()) return 0.0
        val hits = tokens.count { documentText.contains(it) }
        return if (hits == tokens.size && tokens.size >= 2) 0.75 else 0.0
    }
}
