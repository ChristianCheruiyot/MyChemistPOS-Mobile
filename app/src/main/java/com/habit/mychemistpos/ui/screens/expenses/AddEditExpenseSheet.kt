package com.habit.mychemistpos.ui.screens.expenses

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
import com.habit.mychemistpos.data.model.Expense
import com.habit.mychemistpos.data.model.ExpenseCategories
import com.habit.mychemistpos.ui.components.ChemistDropdown
import com.habit.mychemistpos.ui.components.ChemistPrimaryButton
import com.habit.mychemistpos.ui.components.ChemistTextField
import com.habit.mychemistpos.ui.components.SectionHeader
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseSheet(
    expense: Expense?,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = expense != null

    var description by remember { mutableStateOf(expense?.description ?: "") }
    var amount by remember { mutableStateOf(if (expense != null && expense.amount > 0) expense.amount.toString() else "") }
    var category by remember { mutableStateOf(expense?.category ?: "") }

    // ── Validation ──────────────────────────────────────────────────────────
    var hasAttemptedSave by remember { mutableStateOf(false) }
    val categoryError = hasAttemptedSave && category.isBlank()
    val descriptionError = hasAttemptedSave && description.isBlank()
    val amountError = hasAttemptedSave && ((amount.toDoubleOrNull() ?: 0.0) <= 0)
    val isFormValid = category.isNotBlank() && description.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0

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
                text = if (isEditing) "Edit Expense" else "Record Expense",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )

            Spacer(Modifier.height(Dimens.SpacingXxl))

            // ── Category Section ────────────────────────────────────────────
            SectionHeader(title = "Category", showDivider = false)
            Spacer(Modifier.height(Dimens.SpacingS))

            ChemistDropdown(
                selectedValue = category,
                onValueChange = { category = it },
                options = ExpenseCategories,
                label = "Expense Category *",
                isError = categoryError,
                errorMessage = if (categoryError) "Select a category" else null
            )

            // ── Details Section ─────────────────────────────────────────────
            Spacer(Modifier.height(Dimens.SpacingL))
            SectionHeader(title = "Details")
            Spacer(Modifier.height(Dimens.SpacingS))

            ChemistTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description *",
                placeholder = "What is this expense for?",
                singleLine = false,
                maxLines = 3,
                isError = descriptionError,
                errorMessage = if (descriptionError) "Description is required" else null
            )

            Spacer(Modifier.height(Dimens.SpacingL))

            ChemistTextField(
                value = amount,
                onValueChange = { amount = it },
                label = "Amount (KES) *",
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError,
                errorMessage = if (amountError) "Enter a valid amount > 0" else null
            )

            // ── Save ────────────────────────────────────────────────────────
            Spacer(Modifier.height(Dimens.SpacingXxl))

            ChemistPrimaryButton(
                text = if (isEditing) "Update Expense" else "Save Expense",
                onClick = {
                    hasAttemptedSave = true
                    if (isFormValid) {
                        onSave(
                            (expense ?: Expense()).copy(
                                description = description.trim(),
                                category = category,
                                amount = amount.toDoubleOrNull() ?: 0.0
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
