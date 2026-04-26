package com.aicrm.repository

import com.aicrm.domain.RagService
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class RagServiceRepository(private val jdbc: JdbcTemplate) {

    fun findAll(region: String? = null): List<RagService> =
        if (region != null) {
            jdbc.query(
                """SELECT s.*, c.display_name AS category_display_name
                   FROM aicrm_picolabbs_rag_services s
                   LEFT JOIN aicrm_picolabbs_rag_category c ON c.code = s.category
                   WHERE s.region = ? ORDER BY s.created_at DESC""",
                rowMapper, region
            )
        } else {
            jdbc.query(
                """SELECT s.*, c.display_name AS category_display_name
                   FROM aicrm_picolabbs_rag_services s
                   LEFT JOIN aicrm_picolabbs_rag_category c ON c.code = s.category
                   ORDER BY s.created_at DESC""",
                rowMapper
            )
        }

    fun searchByRegionAndKeyword(region: String?, keyword: String?): List<RagService> {
        val k = keyword?.let { "%${it.lowercase()}%" }
        if (region != null && k != null) {
            return jdbc.query(
                """SELECT s.*, c.display_name AS category_display_name
                   FROM aicrm_picolabbs_rag_services s
                   LEFT JOIN aicrm_picolabbs_rag_category c ON c.code = s.category
                   WHERE s.region = ? AND (LOWER(s.name) LIKE LOWER(?) OR LOWER(COALESCE(s.description,'')) LIKE LOWER(?))
                   ORDER BY s.created_at DESC""",
                rowMapper, region, "%$keyword%", "%$keyword%"
            )
        }
        if (region != null) return findAll(region)
        if (k != null) {
            return jdbc.query(
                """SELECT s.*, c.display_name AS category_display_name
                   FROM aicrm_picolabbs_rag_services s
                   LEFT JOIN aicrm_picolabbs_rag_category c ON c.code = s.category
                   WHERE LOWER(s.name) LIKE LOWER(?) OR LOWER(COALESCE(s.description,'')) LIKE LOWER(?)
                   ORDER BY s.created_at DESC""",
                rowMapper, "%$keyword%", "%$keyword%"
            )
        }
        return findAll()
    }

    fun insert(s: RagService) {
        jdbc.update(
            "INSERT INTO aicrm_picolabbs_rag_services (id, name, description, region, category) VALUES (?, ?, ?, ?, ?)",
            s.id, s.name, s.description, s.region, s.category
        )
    }

    fun updateCategoryName(oldName: String, newName: String) {
        jdbc.update("UPDATE aicrm_picolabbs_rag_services SET category = ? WHERE category = ?", newName, oldName)
    }

    fun deleteByCategory(categoryName: String) {
        jdbc.update("DELETE FROM aicrm_picolabbs_rag_services WHERE category = ?", categoryName)
    }

    fun deleteAll() {
        jdbc.update("DELETE FROM aicrm_picolabbs_rag_services")
    }

    private val rowMapper = RowMapper { rs, _ ->
        RagService(
            id = rs.getString("id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
            region = rs.getString("region"),
            category = rs.getString("category"),
            categoryDisplayName = rs.getString("category_display_name"),
            createdAt = rs.getTimestamp("created_at").toInstant()
        )
    }
}
