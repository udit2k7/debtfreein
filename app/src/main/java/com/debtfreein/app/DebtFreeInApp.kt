package com.debtfreein.app

import android.app.Application
import android.content.Intent
import android.util.Log

class DebtFreeInApp : Application() {
    override fun onCreate() {
        super.onCreate()
        com.debtfreein.app.data.security.TokenManager.initialize(this)
        
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            val stackTrace = Log.getStackTraceString(exception)
            Log.e("DebtFreeInApp", "FATAL EXCEPTION: ", exception)
            
            try {
                val intent = Intent(this, CrashReportActivity::class.java).apply {
                    putExtra("stack_trace", stackTrace)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DebtFreeInApp", "Failed to start CrashReportActivity", e)
            }
            
            android.os.Process.killProcess(android.os.Process.myPid())
            java.lang.System.exit(10)
        }
    }
}
