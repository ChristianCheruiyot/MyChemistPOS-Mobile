package com.habit.mychemistpos.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habit.mychemistpos.ui.theme.Border
import com.habit.mychemistpos.ui.theme.Danger
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.PrimaryEnd
import com.habit.mychemistpos.ui.theme.PrimaryStart
import com.habit.mychemistpos.ui.theme.Success
import com.habit.mychemistpos.ui.theme.Surface
import com.habit.mychemistpos.ui.theme.TextHint
import com.habit.mychemistpos.ui.theme.TextPrimary
import com.habit.mychemistpos.ui.theme.TextSecondary
import com.habit.mychemistpos.viewmodel.LicenseViewModel

@Composable
fun LicenseScreen(
    viewModel: LicenseViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var keyInput by remember { mutableStateOf("") }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showContent = true
    }

    val context = LocalContext.current

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
            // Header Section
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

            // Input Card Section
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
                        text = "Activate License",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(Dimens.SpacingS))
                    Text(
                        text = "Enter your license key to activate POS features.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextHint,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(Dimens.SpacingXxxl))

                    // Outlined Text Field for License Key Input
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { input ->
                            val cleanInput = input.uppercase()
                            keyInput = cleanInput
                            viewModel.clearError()
                        },
                        label = { Text("License Key") },
                        placeholder = { Text("CHEM-XXXX-XXXX-XXXX") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Key,
                                contentDescription = null,
                                tint = TextHint
                            )
                        },
                        shape = RoundedCornerShape(Dimens.InputCorner),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Border,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = TextHint,
                            cursorColor = Primary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    )

                    Spacer(Modifier.height(Dimens.SpacingXl))

                    // Error Display
                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Danger,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.SpacingM)
                        )
                    }

                    // Success Display
                    if (state.successMessage != null) {
                        Text(
                            text = state.successMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Success,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.SpacingM)
                        )
                    }

                    // Activate Button
                    Button(
                        onClick = {
                            viewModel.activateLicense(keyInput)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.ButtonHeight),
                        shape = RoundedCornerShape(Dimens.ButtonCorner),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Color.White
                        ),
                        enabled = !state.isLoading && keyInput.trim().isNotEmpty()
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Activate Device",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(Dimens.SpacingXl))
                }
            }
        }
    }
}
