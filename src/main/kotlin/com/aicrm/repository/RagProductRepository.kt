package com.aicrm.repository

import com.aicrm.domain.RagProduct
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class RagProductRepository(private val jdbc: JdbcTemplate) {

    fun findAll(region: String? = null): List<RagProduct> =
        if (region != null) {
            jdbc.query(
                """SELECT p.*, c.display_name AS category_display_name
                   FROM aicrm_picolabbs_rag_products p
                   LEFT JOIN aicrm_picolabbs_rag_category c ON c.code = p.category
                   WHERE p.region = ? ORDER BY p.created_at DESC""",
                rowMapper, region
            )
        } else {
            jdbc.query(
                """SELECT p.*, c.display_name AS category_display_name
                   FROM aicrm_picolabbs_rag_products p
                   LEFT JOIN aicrm_picolabbs_rag_category c ON c.code = p.category
                   ORDER BY p.created_at DESC""",
                rowMapper
            )
        }

    fun searchByRegionAndKeyword(region: String?, keyword: String?): List<RagProduct> {
        val keywordParam = keyword?.let { "%$it%" }
        if (region != null && keywordParam != null) {
            return jdbc.query(
                """SELECT p.*, c.display_name AS category_display_name
                   FROM aicrm_picolabbs_rag_products p
                   LEFT JOIN aicrm_picolabbs_rag_category c ON c.code = p.category
                   WHERE p.region = ? AND (LOWER(p.name) LIKE LOWER(?) OR LOWER(COALESCE(p.description,'')) LIKE LOWER(?))
                   ORDER BY p.created_at DESC""",
                rowMapper, region, keywordParam, keywordParam
            )
        }
        if (region != null) return findAll(region)
        if (keywordParam != null) {
            return jdbc.query(
                """SELECT p.*, c.display_name AS category_display_name
                   FROM aicrm_picolabbs_rag_products p
                   LEFT JOIN aicrm_picolabbs_rag_category c ON c.code = p.category
                   WHERE LOWER(p.name) LIKE LOWER(?) OR LOWER(COALESCE(p.description,'')) LIKE LOWER(?)
                   ORDER BY p.created_at DESC""",
                rowMapper, keywordParam, keywordParam
            )
        }
        return findAll()
    }

    fun insert(p: RagProduct) {
        jdbc.update(
            "INSERT INTO aicrm_picolabbs_rag_products (id, name, description, region, category) VALUES (?, ?, ?, ?, ?)",
            p.id, p.name, p.description, p.region, p.category
        )
    }

    fun update(id: String, name: String, description: String?, region: String, category: String?) {
        jdbc.update(
            "UPDATE aicrm_picolabbs_rag_products SET name = ?, description = ?, region = ?, category = ? WHERE id = ?",
            name, description, region, category, id
        )
    }

    fun deleteById(id: String) {
        jdbc.update("DELETE FROM aicrm_picolabbs_rag_products WHERE id = ?", id)
    }

    fun updateCategoryName(oldName: String, newName: String) {
        jdbc.update("UPDATE aicrm_picolabbs_rag_products SET category = ? WHERE category = ?", newName, oldName)
    }

    fun deleteCategory(categoryName: String) {
        jdbc.update("DELETE FROM aicrm_picolabbs_rag_products WHERE category = ?", categoryName)
    }

    fun deleteAll() {
        jdbc.update("DELETE FROM aicrm_picolabbs_rag_products")
    }

    private val rowMapper = RowMapper { rs, _ ->
        RagProduct(
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
