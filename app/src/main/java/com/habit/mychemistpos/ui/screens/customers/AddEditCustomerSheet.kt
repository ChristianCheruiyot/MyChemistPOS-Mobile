package com.habit.mychemistpos.ui.screens.customers

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
import androidx.compose.ui.text.input.KeyboardType
import com.habit.mychemistpos.data.model.Customer
import com.habit.mychemistpos.ui.components.ChemistPrimaryButton
import com.habit.mychemistpos.ui.components.ChemistTextField
import com.habit.mychemistpos.ui.components.SectionHeader
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerSheet(
    customer: Customer?,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = customer != null

    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var creditBalance by remember { 
        mutableStateOf(
            if (customer != null && customer.creditBalance > 0) {
                if (customer.creditBalance % 1.0 == 0.0) customer.creditBalance.toLong().toString() else customer.creditBalance.toString()
            } else "0"
        ) 
    }

    // ── Validation ──────────────────────────────────────────────────────────
    var hasAttemptedSave by remember { mutableStateOf(false) }
    val nameError = hasAttemptedSave && name.isBlank()
    val phoneError = hasAttemptedSave && phone.isNotBlank() && phone.trim().length < 10
    val creditError = hasAttemptedSave && creditBalance.isNotBlank() && ((creditBalance.toDoubleOrNull() ?: -1.0) < 0.0)
    val isFormValid = name.isNotBlank() && (phone.isBlank() || phone.trim().length >= 10) && (creditBalance.isBlank() || (creditBalance.toDoubleOrNull() ?: -1.0) >= 0.0)

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
                text = if (isEditing) "Edit Customer" else "Add Customer",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )

            Spacer(Modifier.height(Dimens.SpacingXxl))

            // ── Personal Info ───────────────────────────────────────────────
            SectionHeader(title = "Personal Info", showDivider = false)
            Spacer(Modifier.height(Dimens.SpacingS))

            ChemistTextField(
                value = name,
                onValueChange = { name = it },
                label = "Customer Name *",
                placeholder = "Full name",
                isError = nameError,
                errorMessage = if (nameError) "Name is required" else null
            )

            Spacer(Modifier.height(Dimens.SpacingL))

            ChemistTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone Number",
                placeholder = "e.g., 0712345678",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError,
                errorMessage = if (phoneError) "Enter a valid phone number" else null
            )

            // ── Credit ─────────────────────────────────────────────────────
            Spacer(Modifier.height(Dimens.SpacingL))
            SectionHeader(title = "Credit")
            Spacer(Modifier.height(Dimens.SpacingS))

            ChemistTextField(
                value = creditBalance,
                onValueChange = { creditBalance = it },
                label = "Credit Balance (KES)",
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = creditError,
                errorMessage = if (creditError) "Enter a valid positive number" else null
            )

            // ── Save ────────────────────────────────────────────────────────
            Spacer(Modifier.height(Dimens.SpacingXxl))

            ChemistPrimaryButton(
                text = if (isEditing) "Update Customer" else "Save Customer",
                onClick = {
                    hasAttemptedSave = true
                    if (isFormValid) {
                        onSave(
                            (customer ?: Customer()).copy(
                                name = name.trim(),
                                phone = phone.trim(),
                                creditBalance = creditBalance.toDoubleOrNull() ?: 0.0
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
