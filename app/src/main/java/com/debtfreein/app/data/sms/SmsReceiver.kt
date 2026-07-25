package com.debtfreein.app.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.debtfreein.app.data.db.AppDatabase
import com.debtfreein.app.data.model.Expense
import com.debtfreein.app.data.model.SystemLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (msgs.isNullOrEmpty()) return

        // Reconstruct message from parts
        val smsSender = msgs[0].displayOriginatingAddress ?: "Unknown"
        val smsBodyBuilder = StringBuilder()
        for (msg in msgs) {
            smsBodyBuilder.append(msg.displayMessageBody)
        }
        val smsBody = smsBodyBuilder.toString()
        val timestamp = msgs[0].timestampMillis

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val parsed = SmsParser.parseSms(smsBody)
                if (parsed != null) {
                    val db = AppDatabase.getInstance(context)
                    
                    // Check for deduplication
                    val existingMatches = db.expenseDao().findMatchingExpenses(parsed.amount, timestamp)
                    if (existingMatches.isNotEmpty()) {
                        val existing = existingMatches.first()
                        val newNotes = if (existing.notes.isNullOrEmpty()) {
                            parsed.merchant
                        } else {
                            "${existing.notes}, ${parsed.merchant}"
                        }
                        val updated = existing.copy(
                            status = "Confirmed",
                            notes = newNotes
                        )
                        db.expenseDao().updateExpense(updated)

                        val logMsg = "Merged duplicate SMS from $smsSender. Updated existing transaction ID ${existing.id} with merchant: ${parsed.merchant}"
                        db.systemLogDao().insertLog(
                            SystemLog(message = logMsg, timestamp = timestamp, level = "INFO")
                        )
                    } else {
                        // Look up credit card by last four
                        var matchedCardId: Long? = null
                        if (parsed.cardLastFour != null) {
                            val card = db.cardDao().getCardByLastFour(parsed.cardLastFour)
                            if (card != null) {
                                matchedCardId = card.id
                                // Update card balance
                                val updatedCard = card.copy(
                                    currentBalance = card.currentBalance + parsed.amount
                                )
                                db.cardDao().updateCard(updatedCard)
                            }
                        }

                        // Insert the expense
                        val expense = Expense(
                            amount = parsed.amount,
                            merchant = parsed.merchant,
                            timestamp = timestamp,
                            category = if (matchedCardId != null) "Auto-Card" else "Auto-SMS",
                            cardId = matchedCardId,
                            rawSmsText = smsBody
                        )
                        db.expenseDao().insertExpense(expense)

                        // Log success
                        val logMsg = "Parsed SMS from $smsSender. Amount: ${parsed.amount}, Card Ending: ${parsed.cardLastFour ?: "None"}, Merchant: ${parsed.merchant}"
                        db.systemLogDao().insertLog(
                            SystemLog(message = logMsg, timestamp = timestamp, level = "INFO")
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error parsing SMS: ${e.message}", e)
                try {
                    val db = AppDatabase.getInstance(context)
                    db.systemLogDao().insertLog(
                        SystemLog(
                            message = "Error parsing SMS from $smsSender: ${e.localizedMessage}",
                            timestamp = System.currentTimeMillis(),
                            level = "ERROR"
                        )
                    )
                } catch (dbEx: Exception) {
                    // Ignore DB exception during secondary error handling
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
