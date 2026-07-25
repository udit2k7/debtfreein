package com.debtfreein.app.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debtfreein.app.R
import com.debtfreein.app.data.security.SecureStorageManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var isLoading by remember { mutableStateOf(false) }

    // Google Sign-In options
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            isLoading = true
            auth.signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        // Sync keys from firestore
                        SecureStorageManager.fetchSecureKeysFromFirestore { success ->
                            isLoading = false
                            if (success) {
                                Toast.makeText(context, "Welcome back, ${auth.currentUser?.displayName}!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Sign in successful, config initialized.", Toast.LENGTH_SHORT).show()
                            }
                            onLoginSuccess()
                        }
                    } else {
                        isLoading = false
                        Toast.makeText(context, "Firebase authentication failed: ${authResult.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: ApiException) {
            isLoading = false
            Toast.makeText(context, "Google Sign-In failed (Code: ${e.statusCode})", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Elegant Branded Phone Logo Component
            Image(
                painter = painterResource(id = R.drawable.ic_phone_logo),
                contentDescription = "DebtFreeIn Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // App Brand Name & Subtitle
            Text(
                text = "DebtFreeIn",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "Autonomous AI-Driven Debt Liquidation & Trading Intelligence Platform",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFF6366F1),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Authenticating and syncing Cloud Keychain...",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            } else {
                // Premium Styled Google Sign-In Button
                Button(
                    onClick = {
                        isLoading = true
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF10B981))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "G",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6366F1),
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = "Sign in with Google",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Temporary Bypass for Offline testing or Simulator environment
                TextButton(
                    onClick = {
                        isLoading = true
                        // Sign in anonymously to simulate auth session and fetch / set keys
                        auth.signInAnonymously()
                            .addOnCompleteListener { authResult ->
                                if (authResult.isSuccessful) {
                                    SecureStorageManager.fetchSecureKeysFromFirestore {
                                        isLoading = false
                                        onLoginSuccess()
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Anonymous login failed: ${authResult.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "Testing / Offline Mode (Bypass)",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}