package com.habit.mychemistpos.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import com.habit.mychemistpos.ui.theme.Border
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.Surface as ThemeSurface
import com.habit.mychemistpos.ui.theme.TextHint
import com.habit.mychemistpos.ui.theme.TextPrimary
import com.habit.mychemistpos.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ── Section Header ──────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (showDivider) {
            HorizontalDivider(
                thickness = Dimens.DividerThickness,
                color = Border,
                modifier = Modifier.padding(vertical = Dimens.SpacingS)
            )
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = Dimens.SpacingS)
        )
    }
}

// ── Searchable Picker Dialog Dropdown ───────────────────────────────────────────────
@Composable
fun ChemistDropdown(
    selectedValue: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth().animateContentSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Open dropdown",
                        tint = TextHint
                    )
                },
                isError = isError,
                shape = RoundedCornerShape(Dimens.InputCorner),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Border,
                    focusedContainerColor = PageBackground,
                    unfocusedContainerColor = PageBackground,
                    focusedLabelColor = Primary,
                    unfocusedLabelColor = TextHint,
                    cursorColor = Primary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            // Transparent overlay that intercepts all clicks to reliably open selection dialog
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showDialog = true }
            )
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = Dimens.SpacingL, top = Dimens.SpacingXs)
            )
        }
    }

    if (showDialog) {
        CategoryPickerDialog(
            title = label,
            options = options,
            selectedValue = selectedValue,
            onSelect = {
                onValueChange(it)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun CategoryPickerDialog(
    title: String,
    options: List<String>,
    selectedValue: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredOptions = remember(searchQuery, options) {
        if (searchQuery.isBlank()) {
            options
        } else {
            options.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Dimens.CardCorner),
            color = PageBackground,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
        ) {
            Column(
                modifier = Modifier
                    .padding(Dimens.CardPadding)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Select $title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = Dimens.SpacingM)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search or type custom...", color = TextHint) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = TextHint) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, "Clear search", tint = TextHint)
                            }
                        }
                    },
                    shape = RoundedCornerShape(Dimens.InputCorner),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = ThemeSurface,
                        unfocusedContainerColor = ThemeSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Dimens.SpacingM))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs)
                ) {
                    if (searchQuery.isNotBlank() && !options.any { it.equals(searchQuery.trim(), ignoreCase = true) }) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Dimens.InputCorner))
                                    .background(Primary.copy(alpha = 0.08f))
                                    .clickable { onSelect(searchQuery.trim()) }
                                    .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingM)
                            ) {
                                Text(
                                    text = "Add Custom Category:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Primary
                                )
                                Text(
                                    text = searchQuery.trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            HorizontalDivider(color = Border, thickness = Dimens.DividerThickness, modifier = Modifier.padding(vertical = Dimens.SpacingXs))
                        }
                    }

                    items(filteredOptions) { option ->
                        val isSelected = option == selectedValue
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Dimens.InputCorner))
                                .background(if (isSelected) Primary.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { onSelect(option) }
                                .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingM),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) Primary else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (filteredOptions.isEmpty() && searchQuery.isBlank()) {
                        item {
                            Text(
                                text = "No categories available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextHint,
                                modifier = Modifier.padding(Dimens.SpacingM)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpacingM))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        }
    }
}

// ── Date Picker Field ───────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChemistDatePicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Select date",
) {
    var showDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    ) {
        OutlinedTextField(
            value = selectedDate?.format(dateFormatter) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = TextHint) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Pick date",
                    tint = TextHint,
                    modifier = Modifier.height(Dimens.IconSize).width(Dimens.IconSize),
                )
            },
            shape = RoundedCornerShape(Dimens.InputCorner),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border,
                focusedContainerColor = PageBackground,
                unfocusedContainerColor = PageBackground,
                focusedLabelColor = Primary,
                unfocusedLabelColor = TextHint,
                cursorColor = Primary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        // Transparent overlay that intercepts all clicks to reliably open selection dialog
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    }
                    showDialog = false
                }) {
                    Text("OK", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ── Inline Error Helper ─────────────────────────────────────────────────────────
@Composable
fun FormFieldError(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.padding(start = Dimens.SpacingL, top = Dimens.SpacingXs)
    )
}
