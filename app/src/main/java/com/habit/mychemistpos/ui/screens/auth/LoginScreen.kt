package com.habit.mychemistpos.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.width
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import com.habit.mychemistpos.R
import androidx.compose.ui.graphics.Brush
import com.habit.mychemistpos.ui.theme.Danger
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.PrimaryStart
import com.habit.mychemistpos.ui.theme.PrimaryEnd
import com.habit.mychemistpos.ui.theme.Surface
import com.habit.mychemistpos.ui.theme.TextHint
import com.habit.mychemistpos.ui.theme.Border
import com.habit.mychemistpos.ui.theme.TextPrimary
import com.habit.mychemistpos.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var showContent by remember { mutableStateOf(false) }
    var isGoogleSigningIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { showContent = true }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            isGoogleSigningIn = false
        }
    }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoginSuccess()
    }

    // Use Box as root to avoid ColumnScope shadowing AnimatedVisibility
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryStart, PrimaryEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = Dimens.SpacingL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxl)
        ) {
            // ── Branded Header ───────────────────────────────────────────
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.LocalPharmacy,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.height(Dimens.SpacingM))
                    Text(
                        text = "MyChemist POS",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Spacer(Modifier.height(Dimens.SpacingS))
                    Text(
                        text = "Pharmacy Management System",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // ── Login Card (centered floating card) ──────────────────────
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 200)) +
                        slideInVertically(tween(600, delayMillis = 200)) { 40 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Surface,
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = Dimens.SpacingXxl, vertical = Dimens.SpacingXxxl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(Dimens.SpacingS))
                    Text(
                        text = "Sign in to manage your pharmacy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextHint
                    )

                    Spacer(Modifier.height(Dimens.SpacingXxxl))

                    val context = LocalContext.current
                    val coroutineScope = rememberCoroutineScope()
                    val webClientId = stringResource(id = R.string.default_web_client_id)
                    val isButtonLoading = state.isLoading || isGoogleSigningIn

                    Button(
                        onClick = {
                            if (!isButtonLoading) {
                                isGoogleSigningIn = true
                                coroutineScope.launch {
                                    try {
                                        val credentialManager = CredentialManager.create(context)
                                        
                                        val googleIdOption = GetGoogleIdOption.Builder()
                                            .setFilterByAuthorizedAccounts(false)
                                            .setServerClientId(webClientId)
                                            .setAutoSelectEnabled(false)
                                            .build()

                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()

                                        val result = credentialManager.getCredential(
                                            request = request,
                                            context = context,
                                        )
                                        
                                        val credential = result.credential
                                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                            viewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                                        } else {
                                            isGoogleSigningIn = false
                                        }
                                    } catch (e: Exception) {
                                        isGoogleSigningIn = false
                                        if (e !is androidx.credentials.exceptions.GetCredentialCancellationException) {
                                            viewModel.setErrorMessage("Google Sign In error: ${e.localizedMessage ?: e.message ?: "Unknown error"}")
                                        }
                                        e.printStackTrace()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.ButtonHeight),
                        shape = RoundedCornerShape(Dimens.ButtonCorner),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                        enabled = !isButtonLoading
                    ) {
                        if (isButtonLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google),
                                    contentDescription = "Google Logo",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(Modifier.width(Dimens.SpacingM))
                                Text(
                                    text = "Sign in with Google",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    // Error message
                    if (state.error != null) {
                        Spacer(Modifier.height(Dimens.SpacingL))
                        Text(
                            text = state.error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Danger,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(Dimens.SpacingXxl))
                }
            }
        }
    }
}
