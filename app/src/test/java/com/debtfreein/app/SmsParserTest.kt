package com.debtfreein.app

import com.debtfreein.app.data.sms.SmsParser
import org.junit.Assert.*
import org.junit.Test

class SmsParserTest {

    @Test
    fun testParseSms_hdfcCreditCard() {
        val sms = "Alert: Your HDFC Bank Credit Card ending in 4321 was spent for Rs. 1500.00 at AMAZON on 2026-07-14. Bal: Rs. 45000."
        val parsed = SmsParser.parseSms(sms)
        assertNotNull(parsed)
        assertEquals(1500.0, parsed!!.amount, 0.001)
        assertEquals("4321", parsed.cardLastFour)
        assertEquals("AMAZON", parsed.merchant)
    }

    @Test
    fun testParseSms_sbiCreditCard() {
        val sms = "Thank you for using SBI Card ending in 8901 for Rs. 543.00 at Walmart on 14/07/26."
        val parsed = SmsParser.parseSms(sms)
        assertNotNull(parsed)
        assertEquals(543.0, parsed!!.amount, 0.001)
        assertEquals("8901", parsed.cardLastFour)
        assertEquals("Walmart", parsed.merchant)
    }

    @Test
    fun testParseSms_axisBankMultiLine() {
        val sms = """
            Debit
            INR 1241.65
            A/c no. XX4729
            15-11-21 12:59:27
            UPI/P2M/131918633914/www.futur/HDFC BANK
            Bal INR 34316.49
            SMS BLOCKUPI Cust ID to 8691000002, if not you-Axis Bank
        """.trimIndent()
        val parsed = SmsParser.parseSms(sms)
        assertNotNull(parsed)
        assertEquals(1241.65, parsed!!.amount, 0.001)
        assertEquals("4729", parsed.cardLastFour)
        assertEquals("www.futur", parsed.merchant)
    }

    @Test
    fun testParseSms_nonTransaction() {
        val sms = "Dear customer, your OTP for login is 987654. Do not share it with anyone."
        val parsed = SmsParser.parseSms(sms)
        assertNull(parsed)
    }

    @Test
    fun testParseSms_blacklistOtp() {
        val sms = "Your OTP is 123456 for transaction of Rs. 1500.00 at AMAZON."
        assertNull(SmsParser.parseSms(sms))
    }

    @Test
    fun testParseSms_blacklistOneTimePassword() {
        val sms = "One Time Password for transaction of Rs. 1500.00 at AMAZON is 987654."
        assertNull(SmsParser.parseSms(sms))
    }

    @Test
    fun testParseSms_blacklistVerification() {
        val sms = "Your verification code is 1234 for purchase of Rs. 1500.00 at AMAZON."
        assertNull(SmsParser.parseSms(sms))
    }

    @Test
    fun testParseSms_blacklistCode() {
        val sms = "Use code 9988 to complete your purchase of Rs. 1500.00 at AMAZON."
        assertNull(SmsParser.parseSms(sms))
    }
}
