package com.aicrm.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/auth")
@RestController
class AuthController(private val jdbc: JdbcTemplate) {

    @PostMapping("/login")
    fun login(@RequestBody body: Map<String, Any?>): ResponseEntity<Any> {
        val username = body["username"]?.toString() ?: return ResponseEntity.badRequest().body(mapOf("error" to "Missing username"))
        val password = body["password"]?.toString() ?: return ResponseEntity.badRequest().body(mapOf("error" to "Missing password"))

        return try {
            val user = jdbc.queryForMap(
                """
                SELECT l.id, l.username, l.fullname, u.email, u.role, u.user_role, u.permission, u.function
                FROM public.aicrm_picolabbs_login l
                JOIN public.aicrm_picolabbs_user u ON l.id = u.id
                WHERE l.username = ? 
                  AND l.password_hash = crypt(?, l.password_hash) 
                  AND l.is_active = true
                """.trimIndent(),
                username, password
            )
            
            val responseUser = mapOf(
                "id" to user["id"],
                "username" to user["username"],
                "name" to user["fullname"],
                "email" to user["email"],
                "role" to user["role"], // legacy 'operator' or 'superadmin'
                "user_role" to user["user_role"], // 'master_admin' or 'sales'
                "permission" to user["permission"],
                "function" to user["function"]
            )
            ResponseEntity.ok(mapOf("user" to responseUser, "token" to "dummy-jwt-token-for-demo-purposes"))
        } catch (e: org.springframework.dao.EmptyResultDataAccessException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Invalid credentials"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to e.message))
        }
    }
}
