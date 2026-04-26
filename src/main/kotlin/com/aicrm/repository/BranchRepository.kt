package com.aicrm.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class BranchRepository(private val jdbc: JdbcTemplate) {

    fun findAll(region: String? = null): List<Map<String, Any?>> {
        val sql = if (region.isNullOrBlank()) {
            """SELECT id, COALESCE(region,'hk') AS region,
                      COALESCE(NULLIF(name_zh,''), name) AS name_zh,
                      COALESCE(NULLIF(name_en,''), name) AS name_en,
                      COALESCE(NULLIF(district_zh,''), district) AS district_zh,
                      COALESCE(NULLIF(district_en,''), district) AS district_en,
                      COALESCE(NULLIF(address_zh,''), address) AS address_zh,
                      COALESCE(NULLIF(address_en,''), address) AS address_en,
                      phone, whatsapp, hours_zh, hours_en, sort_order
               FROM aicrm_picolabbs_branches
               WHERE COALESCE(NULLIF(whatsapp, ''), '') <> ''
               ORDER BY sort_order ASC, name_zh ASC"""
        } else {
            """SELECT id, COALESCE(region,'hk') AS region,
                      COALESCE(NULLIF(name_zh,''), name) AS name_zh,
                      COALESCE(NULLIF(name_en,''), name) AS name_en,
                      COALESCE(NULLIF(district_zh,''), district) AS district_zh,
                      COALESCE(NULLIF(district_en,''), district) AS district_en,
                      COALESCE(NULLIF(address_zh,''), address) AS address_zh,
                      COALESCE(NULLIF(address_en,''), address) AS address_en,
                      phone, whatsapp, hours_zh, hours_en, sort_order
               FROM aicrm_picolabbs_branches
               WHERE region = ?
                 AND COALESCE(NULLIF(whatsapp, ''), '') <> ''
               ORDER BY sort_order ASC, name_zh ASC"""
        }
        val rows = if (region.isNullOrBlank()) jdbc.queryForList(sql) else jdbc.queryForList(sql, region)
        return rows.map { row ->
            mapOf(
                "id" to row["id"],
                "region" to row["region"],
                "name_zh" to row["name_zh"],
                "name_en" to row["name_en"],
                "district_zh" to row["district_zh"],
                "district_en" to row["district_en"],
                "address_zh" to row["address_zh"],
                "address_en" to row["address_en"],
                "phone" to row["phone"],
                "whatsapp" to row["whatsapp"],
                "hours_zh" to row["hours_zh"],
                "hours_en" to row["hours_en"],
                "sort_order" to row["sort_order"]
            )
        }
    }
}
