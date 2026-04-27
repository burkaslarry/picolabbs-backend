package com.aicrm.util

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/** Legacy random id; prefer [nextRuntimeLeadId] / [nextRuntimeAuxId] for CRM rows. */
fun uuid(): String = UUID.randomUUID().toString()

/**
 * API / WhatsApp 新建查詢 — 純數字 id，與 seed 區間（100001–100020、88001001–88001004）分開。
 */
private val runtimeLeadSeq = AtomicLong(1_000_000_001L)

/** Timeline、slot、scheduled job 等附屬列 — 與 seed（890M／891M）及 automation（9520M+）分開。 */
private val runtimeAuxSeq = AtomicLong(7_100_000_001L)

fun nextRuntimeLeadId(): String = runtimeLeadSeq.getAndIncrement().toString()

fun nextRuntimeAuxId(): String = runtimeAuxSeq.getAndIncrement().toString()
