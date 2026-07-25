package com.debtfreein.app

import com.debtfreein.app.data.model.Expense
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class DeduplicationTest {

    // A list simulating our database of expenses
    private val databaseExpenses = mutableListOf<Expense>()

    // Simulated Dao/Repository function to find matching expenses in the last 15 minutes (900,000 ms)
    private fun findMatchingExpenses(amount: Double, timestamp: Long): List<Expense> {
        return databaseExpenses.filter {
            it.amount == amount && abs(it.timestamp - timestamp) <= 900000
        }.sortedBy { abs(it.timestamp - timestamp) }
    }

    // Simulated SMS processing logic to test the merge / deduplication path
    private fun processSmsTransaction(amount: Double, merchant: String, timestamp: Long, smsBody: String): Boolean {
        val existingMatches = findMatchingExpenses(amount, timestamp)
        if (existingMatches.isNotEmpty()) {
            val existing = existingMatches.first()
            val newNotes = if (existing.notes.isNullOrEmpty()) {
                merchant
            } else {
                "${existing.notes}, $merchant"
            }
            // Update in place
            val idx = databaseExpenses.indexOf(existing)
            databaseExpenses[idx] = existing.copy(
                status = "Confirmed",
                notes = newNotes
            )
            return false // Match found, merged, not inserted new row
        } else {
            databaseExpenses.add(
                Expense(
                    id = (databaseExpenses.size + 1).toLong(),
                    amount = amount,
                    merchant = merchant,
                    timestamp = timestamp,
                    category = "Auto-SMS",
                    cardId = null,
                    rawSmsText = smsBody,
                    status = null,
                    notes = null
                )
            )
            return true // Inserted new row
        }
    }

    @Test
    fun testDeduplication_noMatchFound() {
        databaseExpenses.clear()
        
        // 1. First transaction
        val inserted1 = processSmsTransaction(150.0, "HDFC Debit", 1000000L, "Debit Rs 150 at merchant HDFC")
        assertTrue(inserted1)
        assertEquals(1, databaseExpenses.size)
        assertNull(databaseExpenses[0].status)
        assertNull(databaseExpenses[0].notes)
    }

    @Test
    fun testDeduplication_matchFoundWithin15Minutes() {
        databaseExpenses.clear()

        // 1. Insert original debit transaction
        processSmsTransaction(500.0, "HDFC Debit", 1000000L, "Debit Rs 500 at merchant HDFC")
        
        // 2. Second transaction for same amount 10 minutes later (600,000 ms) - should merge
        val inserted2 = processSmsTransaction(500.0, "Airtel Merchant", 1600000L, "Confirmation for Rs 500 at Airtel")
        assertFalse(inserted2)
        assertEquals(1, databaseExpenses.size) // No new row created
        assertEquals("Confirmed", databaseExpenses[0].status)
        assertEquals("Airtel Merchant", databaseExpenses[0].notes)

        // 3. Third transaction for same amount 4 minutes later (240,000 ms) - should merge and append to notes
        val inserted3 = processSmsTransaction(500.0, "Payment Gateway", 1840000L, "Gateway confirmation Rs 500")
        assertFalse(inserted3)
        assertEquals(1, databaseExpenses.size)
        assertEquals("Confirmed", databaseExpenses[0].status)
        assertEquals("Airtel Merchant, Payment Gateway", databaseExpenses[0].notes)
    }

    @Test
    fun testDeduplication_noMatchOutside15Minutes() {
        databaseExpenses.clear()

        // 1. Insert original debit transaction
        processSmsTransaction(500.0, "HDFC Debit", 1000000L, "Debit Rs 500 at merchant HDFC")

        // 2. Second transaction for same amount 20 minutes later (1,200,000 ms) - should NOT merge
        val inserted2 = processSmsTransaction(500.0, "Airtel Merchant", 2200000L, "Confirmation for Rs 500 at Airtel")
        assertTrue(inserted2)
        assertEquals(2, databaseExpenses.size) // New row created
        assertNull(databaseExpenses[1].status)
        assertNull(databaseExpenses[1].notes)
    }
}
