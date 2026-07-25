package com.debtfreein.app.data.sms

import java.util.regex.Pattern

data class ParsedTransaction(
    val amount: Double,
    val cardLastFour: String?,
    val merchant: String
)

object SmsParser {
    // Single robust amount pattern that matches Rs, INR, ₹ with or without commas and decimals
    private val amountPattern = Pattern.compile(
        "(?i)(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)"
    )

    // Card pattern matching A/C, card, account, xx etc. followed by 4-8 digits
    private val cardPattern = Pattern.compile(
        "(?i)(?:a/c|card|acct|ending|ends|xx|x)\\s*(?:ending\\s+in|ending|no\\.?|x+|-|in)?\\s*(\\d{4,8})\\b"
    )

    // Regex patterns for different merchant context keywords
    private val upiPattern = Pattern.compile("(?i)upi/(?:p2m|p2a|p2p)/\\d+/([^/]+)")
    private val atPattern = Pattern.compile("(?i)\\bat\\s+([A-Za-z0-9\\s&/]+)")
    private val towardsPattern = Pattern.compile("(?i)towards\\s+([A-Za-z0-9\\s&/]+)")
    private val toPattern = Pattern.compile("(?i)\\bto\\s+([A-Za-z0-9\\s&/]+)")

    fun parseSms(body: String): ParsedTransaction? {
        if (body.isBlank()) return null

        val lowerBody = body.lowercase()
        if (lowerBody.contains("otp") ||
            lowerBody.contains("one time password") ||
            lowerBody.contains("verification") ||
            lowerBody.contains("code")
        ) {
            return null
        }

        // 1. Extract Amount
        val amountMatcher = amountPattern.matcher(body)
        var amount: Double? = null
        
        while (amountMatcher.find()) {
            val startIdx = amountMatcher.start()
            // Look back up to 15 chars to check if this amount represents a Balance/Limit (e.g. avl bal, limit)
            val lookbackStart = maxOf(0, startIdx - 15)
            val lookback = body.substring(lookbackStart, startIdx).lowercase()
            
            if (lookback.contains("bal") || lookback.contains("lmt") || lookback.contains("limit")) {
                continue
            }
            
            val matchedStr = amountMatcher.group(1)
            if (matchedStr != null) {
                val cleanVal = matchedStr.replace(",", "")
                val parsed = cleanVal.toDoubleOrNull()
                if (parsed != null) {
                    amount = parsed
                    break
                }
            }
        }

        // Fallback: If no transaction amount was found (e.g. lookback didn't catch anything, or all were marked balance),
        // take the first matched amount as fallback
        if (amount == null) {
            amountMatcher.reset()
            if (amountMatcher.find()) {
                val matchedStr = amountMatcher.group(1)
                if (matchedStr != null) {
                    val cleanVal = matchedStr.replace(",", "")
                    amount = cleanVal.toDoubleOrNull()
                }
            }
        }

        if (amount == null || amount <= 0.0) return null

        // 2. Extract Card / Account Last 4 Digits
        var cardLastFour: String? = null
        val cardMatcher = cardPattern.matcher(body)
        if (cardMatcher.find()) {
            val valStr = cardMatcher.group(1)
            if (valStr != null && valStr.length >= 4) {
                cardLastFour = valStr.takeLast(4)
            }
        }

        // 3. Extract Merchant Name
        val merchant = extractMerchantName(body)

        return ParsedTransaction(
            amount = amount,
            cardLastFour = cardLastFour,
            merchant = merchant
        )
    }

    private fun extractMerchantName(body: String): String {
        // A. UPI/P2M or UPI/P2A
        val upiMatcher = upiPattern.matcher(body)
        if (upiMatcher.find()) {
            val match = upiMatcher.group(1)
            if (!match.isNullOrBlank()) {
                return match.trim()
            }
        }

        // B. spent on ... at <MERCHANT>
        val atMatcher = atPattern.matcher(body)
        if (atMatcher.find()) {
            val match = atMatcher.group(1)
            if (!match.isNullOrBlank()) {
                val processed = processMerchantSegment(match)
                if (processed != null) return processed
            }
        }

        // C. debited towards <MERCHANT>
        val towardsMatcher = towardsPattern.matcher(body)
        if (towardsMatcher.find()) {
            val match = towardsMatcher.group(1)
            if (!match.isNullOrBlank()) {
                val processed = processMerchantSegment(match)
                if (processed != null) return processed
            }
        }

        // D. to <MERCHANT>
        val toMatcher = toPattern.matcher(body)
        if (toMatcher.find()) {
            val match = toMatcher.group(1)
            if (!match.isNullOrBlank()) {
                val processed = processMerchantSegment(match)
                if (processed != null) {
                    val words = processed.split(Pattern.compile("\\s+"))
                    if (words.isNotEmpty() && words[0].lowercase() !in setOf("your", "avoid", "report", "be")) {
                        return processed
                    }
                }
            }
        }

        return "Unknown Merchant"
    }

    private fun processMerchantSegment(segment: String): String? {
        val trimmed = segment.trim()
        val parts = trimmed.split("/").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return null
        
        var merchantPart = parts[0]
        // Skip common Indian bank transaction prefixes if secondary parts exist
        if (merchantPart.uppercase() in setOf("PUR", "POS", "IPS", "UPI") && parts.size > 1) {
            merchantPart = parts[1]
        }
        
        val words = merchantPart.split(Pattern.compile("\\s+"))
        val stopWords = setOf(
            "on", "at", "for", "by", "in", "via", "against", "from", 
            "using", "card", "ending", "was", "made", "to", "with", 
            "balance", "bal", "avl", "available", "limit", "lmt", "rs", "inr"
        )
        
        val merchantWords = mutableListOf<String>()
        for (word in words) {
            val cleanWord = word.replace(Regex("[^A-Za-z0-9\\-&\\*]"), "")
            if (cleanWord.lowercase() in stopWords || cleanWord.isEmpty() || cleanWord.matches(Regex("\\d{4}")) || cleanWord.contains(Regex("\\d{2}[/-]\\d{2}"))) {
                break
            }
            merchantWords.add(cleanWord)
        }
        
        if (merchantWords.isEmpty()) return null
        return merchantWords.take(3).joinToString(" ")
    }
}
