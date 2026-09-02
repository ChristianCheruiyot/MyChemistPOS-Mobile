package com.habit.mychemistpos.ui.screens.suppliers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.habit.mychemistpos.data.model.Supplier
import com.habit.mychemistpos.ui.components.ChemistPrimaryButton
import com.habit.mychemistpos.ui.components.ChemistTextField
import com.habit.mychemistpos.ui.components.SectionHeader
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSupplierSheet(
    supplier: Supplier?,
    onDismiss: () -> Unit,
    onSave: (Supplier) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = supplier != null

    var name by remember { mutableStateOf(supplier?.name ?: "") }
    var phone by remember { mutableStateOf(supplier?.phone ?: "") }
    var email by remember { mutableStateOf(supplier?.email ?: "") }
    var address by remember { mutableStateOf(supplier?.address ?: "") }

    // ── Validation ──────────────────────────────────────────────────────────
    var hasAttemptedSave by remember { mutableStateOf(false) }
    val nameError = hasAttemptedSave && name.isBlank()
    val emailError = hasAttemptedSave && email.isNotBlank() && !email.contains("@")
    val phoneError = hasAttemptedSave && phone.isNotBlank() && phone.length < 10
    val isFormValid = name.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PageBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(bottom = Dimens.SpacingXxl)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = if (isEditing) "Edit Supplier" else "Add Supplier",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )

            Spacer(Modifier.height(Dimens.SpacingXxl))

            // ── Company Info ────────────────────────────────────────────────
            SectionHeader(title = "Company Info", showDivider = false)
            Spacer(Modifier.height(Dimens.SpacingS))

            ChemistTextField(
                value = name,
                onValueChange = { name = it },
                label = "Supplier Name *",
                placeholder = "e.g., PharmaCare Distributors",
                isError = nameError,
                errorMessage = if (nameError) "Name is required" else null
            )

            // ── Contact Details ─────────────────────────────────────────────
            Spacer(Modifier.height(Dimens.SpacingL))
            SectionHeader(title = "Contact Details")
            Spacer(Modifier.height(Dimens.SpacingS))

            ChemistTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone Number",
                placeholder = "e.g., 0712345678",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError,
                errorMessage = if (phoneError) "Enter a valid phone number" else null
            )

            Spacer(Modifier.height(Dimens.SpacingL))

            ChemistTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                placeholder = "e.g., info@supplier.co.ke",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    capitalization = KeyboardCapitalization.None
                ),
                isError = emailError,
                errorMessage = if (emailError) "Enter a valid email address" else null
            )

            // ── Address ─────────────────────────────────────────────────────
            Spacer(Modifier.height(Dimens.SpacingL))
            SectionHeader(title = "Location")
            Spacer(Modifier.height(Dimens.SpacingS))

            ChemistTextField(
                value = address,
                onValueChange = { address = it },
                label = "Physical Address",
                placeholder = "e.g., Industrial Area, Nairobi",
                singleLine = false,
                maxLines = 2
            )

            // ── Save ────────────────────────────────────────────────────────
            Spacer(Modifier.height(Dimens.SpacingXxl))

            ChemistPrimaryButton(
                text = if (isEditing) "Update Supplier" else "Save Supplier",
                onClick = {
                    hasAttemptedSave = true
                    if (isFormValid) {
                        onSave(
                            (supplier ?: Supplier()).copy(
                                name = name.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                address = address.trim()
                            )
                        )
                        onDismiss()
                    }
                },
                enabled = isFormValid || !hasAttemptedSave
            )

            Spacer(Modifier.height(Dimens.SpacingXxl))
        }
    }
}
