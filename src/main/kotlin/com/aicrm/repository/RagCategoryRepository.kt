package com.aicrm.repository

import com.aicrm.domain.RagCategory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class RagCategoryRepository(private val jdbc: JdbcTemplate) {

    fun findAll(): List<RagCategory> = jdbc.query(
        "SELECT code, display_name, created_at FROM aicrm_picolabbs_rag_category ORDER BY display_name ASC, code ASC"
    ) { rs, _ ->
        RagCategory(
            code = rs.getString("code"),
            displayName = rs.getString("display_name"),
            createdAt = rs.getTimestamp("created_at").toInstant()
        )
    }

    fun upsert(code: String, displayName: String) {
        jdbc.update(
            """INSERT INTO aicrm_picolabbs_rag_category (code, display_name)
               VALUES (?, ?)
               ON CONFLICT (code) DO UPDATE SET display_name = EXCLUDED.display_name""",
            code, displayName
        )
    }

    fun renameCode(oldCode: String, newCode: String, displayName: String) {
        if (oldCode == newCode) {
            upsert(newCode, displayName)
            return
        }
        jdbc.update(
            "DELETE FROM aicrm_picolabbs_rag_category WHERE code = ?",
            newCode
        )
        jdbc.update(
            "UPDATE aicrm_picolabbs_rag_category SET code = ?, display_name = ? WHERE code = ?",
            newCode, displayName, oldCode
        )
    }

    fun deleteByCode(code: String) {
        jdbc.update("DELETE FROM aicrm_picolabbs_rag_category WHERE code = ?", code)
    }
}
