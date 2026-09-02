package com.habit.mychemistpos.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habit.mychemistpos.ui.components.ChemistTopBar
import com.habit.mychemistpos.ui.theme.Danger
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.Surface
import com.habit.mychemistpos.ui.theme.TextSecondary
import com.habit.mychemistpos.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    licenseViewModel: com.habit.mychemistpos.viewmodel.LicenseViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    val licenseState by licenseViewModel.uiState.collectAsState()
    val user = authState.user


    Scaffold(
        containerColor = PageBackground,
        topBar = {
            ChemistTopBar(title = "Settings", showBack = true, onBackClick = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(Dimens.SpacingXxl))
            
            // Profile Avatar Placeholder
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = Surface,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Primary)
                    .padding(Dimens.SpacingL)
            )
            
            Spacer(Modifier.height(Dimens.SpacingM))
            Text(user?.name ?: "User", style = MaterialTheme.typography.titleLarge)
            Text(user?.email ?: "email@example.com", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            
            Spacer(Modifier.height(Dimens.SpacingXxl))
            
            // Basic Info
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Business Info", style = MaterialTheme.typography.labelMedium, color = Primary)
                Spacer(Modifier.height(Dimens.SpacingS))
                
                RowSetting(icon = Icons.Outlined.Business, title = "Business Name", value = user?.businessName ?: "MyChemist POS")
                RowSetting(icon = Icons.Outlined.Person, title = "Role", value = user?.role?.name ?: "ADMIN")
            }

            Spacer(Modifier.height(Dimens.SpacingXl))

            // License Details
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("License Details", style = MaterialTheme.typography.labelMedium, color = Primary)
                Spacer(Modifier.height(Dimens.SpacingS))
                
                RowSetting(icon = Icons.Outlined.Key, title = "License Key", value = licenseState.licenseKey.ifEmpty { "Developer Key" })
            }

            Spacer(Modifier.weight(1f))
            
            androidx.compose.material3.OutlinedButton(
                onClick = {
                    authViewModel.logout()
                    licenseViewModel.deactivateLicense()
                },
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                border = androidx.compose.foundation.BorderStroke(1.dp, Danger),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.ButtonCorner),
                modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight)
            ) {
                Text("Deactivate & Unlink License", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(Dimens.SpacingM))

            androidx.compose.material3.Button(
                onClick = {
                    authViewModel.logout()
                    onNavigateToLogin()
                },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.ButtonCorner),
                modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight)
            ) {
                Text("Logout User", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(Dimens.SpacingXl))
        }
    }
}

@Composable
private fun RowSetting(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.padding(Dimens.SpacingM))
        Column {
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
