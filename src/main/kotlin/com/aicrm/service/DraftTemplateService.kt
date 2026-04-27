package com.aicrm.service

import org.springframework.stereotype.Service

@Service
class DraftTemplateService {

    private val safetyDisclaimer = "⚠️ This is not medical advice. If you have severe redness, swelling, breathing difficulty or chest pain, please seek immediate medical attention."

    fun getDraft(vertical: String, intent: String, vars: Map<String, String>): String {
        val v = vertical.ifBlank { "picolabbs_wellness" }
        val name = vars["name"].orEmpty()
        val service = vars["service"].orEmpty().ifBlank {
            when {
                v.startsWith("picolabbs_") -> "our product or service"
                v == "training" -> "our course"
                v == "scheduled" -> "your appointment"
                else -> "our service"
            }
        }
        val location = vars["location"].orEmpty().ifBlank {
            when {
                v.startsWith("picolabbs_") || v == "training" -> "your nearest PicoLabb branch"
                else -> "our clinic"
            }
        }
        val slots = vars["slots"].orEmpty().ifBlank { "Slot 1, Slot 2, Slot 3" }
        val bookingLink = vars["bookingLink"].orEmpty().ifBlank { "[Booking link]" }
        val serviceDate = vars["service_date"].orEmpty().ifBlank { "scheduled date" }

        return when {
            v == "scheduled" -> when (intent) {
                "reminder_2d" -> "Hi${if (name.isNotEmpty()) " $name" else ""}! Reminder: your appointment for $service is in 2 days ($serviceDate). See you soon!"
                "reminder_24h" -> "Hi${if (name.isNotEmpty()) " $name" else ""}! Reminder: your appointment is tomorrow. We look forward to seeing you."
                "feedback_1d" -> "Hi${if (name.isNotEmpty()) " $name" else ""}! Thank you for visiting us. We'd love your feedback — please take 1 min: [Feedback link]. Thank you!"
                else -> getDraft("picolabbs_wellness", if (intent.isBlank()) "info" else intent, vars)
            }
            v.startsWith("picolabbs_") || v == "training" -> when (intent) {
                "book" -> "Hi${if (name.isNotEmpty()) " $name" else ""}! Thanks for your interest in $service. Next intake slots:\n\n$slots\n\nReply with your preference or book here: $bookingLink"
                "price" -> "Hi${if (name.isNotEmpty()) " $name" else ""}! For $service we’ll send our latest options and pricing shortly. Any questions, just reply."
                "info" -> "Hi${if (name.isNotEmpty()) " $name" else ""}! Thanks for reaching out about $service. We’re happy to share details — tell us your preferred branch ($location) and we’ll follow up."
                "complaint" -> "Hi${if (name.isNotEmpty()) " $name" else ""}, we’re sorry for the inconvenience.\n\n$safetyDisclaimer\n\nPlease reply here or WhatsApp us and we’ll arrange someone to assist you."
                else -> "Hi${if (name.isNotEmpty()) " $name" else ""}! Thanks for contacting PicoLabb — happy to help with $service."
            }
            else -> getDraft("picolabbs_wellness", if (intent.isBlank()) "info" else intent, vars)
        }
    }
}
