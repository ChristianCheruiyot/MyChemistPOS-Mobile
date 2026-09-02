package com.habit.mychemistpos.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.habit.mychemistpos.data.model.InventoryItem
import com.habit.mychemistpos.ui.components.ChemistDatePicker
import com.habit.mychemistpos.ui.components.ChemistDropdown
import com.habit.mychemistpos.ui.components.ChemistPrimaryButton
import com.habit.mychemistpos.ui.components.ChemistTextField
import com.habit.mychemistpos.ui.components.ProductImage
import com.habit.mychemistpos.ui.components.ChemistCard
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.PageBackground
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.Surface
import com.habit.mychemistpos.ui.theme.TextPrimary
import com.habit.mychemistpos.ui.theme.TextSecondary
import com.habit.mychemistpos.ui.theme.Border
import com.habit.mychemistpos.ui.theme.Success
import com.habit.mychemistpos.ui.theme.SuccessTint
import com.habit.mychemistpos.ui.theme.Danger
import com.habit.mychemistpos.ui.theme.DangerTint
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemSheet(
    item: InventoryItem?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (InventoryItem) -> Unit,
    onDelete: ((InventoryItem) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = item != null

    // ── Form State ──────────────────────────────────────────────────────────
    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "") }
    var batchNumber by remember { mutableStateOf(item?.batchNumber ?: "") }
    var buyingPrice by remember { mutableStateOf(if (item != null && item.buyingPrice > 0) item.buyingPrice.toString() else "") }
    var sellingPrice by remember { mutableStateOf(if (item != null && item.sellingPrice > 0) item.sellingPrice.toString() else "") }
    var quantity by remember { mutableStateOf(if (item != null) item.quantity.toString() else "") }
    var unit by remember { mutableStateOf(item?.unit ?: "pcs") }
    var reorderLevel by remember { mutableStateOf(item?.reorderLevel?.toString() ?: "10") }
    var expiryDate by remember { mutableStateOf(item?.expiryDate) }
    var requiresPrescription by remember { mutableStateOf(item?.requiresPrescription ?: false) }
    var imageUrl by remember { mutableStateOf(item?.imageUrl) }

    val focusManager = LocalFocusManager.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUrl = uri.toString()
        }
    }

    // ── Pricing Calculations (Profit Margin & Markup) ───────────────────────
    val buyVal = buyingPrice.toDoubleOrNull() ?: 0.0
    val sellVal = sellingPrice.toDoubleOrNull() ?: 0.0
    val profitVal = sellVal - buyVal
    val markupPercent = if (buyVal > 0.0) (profitVal / buyVal) * 100.0 else 0.0
    val marginPercent = if (sellVal > 0.0) (profitVal / sellVal) * 100.0 else 0.0

    // ── Validation State ────────────────────────────────────────────────────
    var hasAttemptedSave by remember { mutableStateOf(false) }
    val nameError = hasAttemptedSave && name.isBlank()
    val categoryError = hasAttemptedSave && category.isBlank()
    val buyingPriceError = hasAttemptedSave && buyingPrice.isNotBlank() && (buyingPrice.toDoubleOrNull() == null || (buyingPrice.toDoubleOrNull() ?: 0.0) < 0)
    val sellingPriceError = hasAttemptedSave && (sellingPrice.isBlank() || (sellingPrice.toDoubleOrNull() ?: 0.0) <= 0)
    val quantityError = hasAttemptedSave && (quantity.isNotBlank() && (quantity.toIntOrNull() == null || (quantity.toIntOrNull() ?: 0) < 0))

    val isFormValid = name.isNotBlank() &&
            category.isNotBlank() &&
            (sellingPrice.toDoubleOrNull() ?: 0.0) > 0 &&
            (buyingPrice.isBlank() || (buyingPrice.toDoubleOrNull() ?: 0.0) >= 0) &&
            (quantity.isBlank() || (quantity.toIntOrNull() ?: 0) >= 0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PageBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(bottom = Dimens.SpacingXxl)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL)
        ) {
            Text(
                text = if (isEditing) "Edit Item" else "Add New Item",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            // ── Card Header: Preset Photo Selector ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.CardCorner))
                    .background(Surface)
                    .border(Dimens.BorderWidth, Border.copy(alpha = 0.8f), RoundedCornerShape(Dimens.CardCorner))
                    .padding(Dimens.CardPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product preview circular frame
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE))
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    ProductImage(
                        imageUrl = imageUrl,
                        contentDescription = "Product Image",
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Upload from gallery",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(Modifier.width(Dimens.SpacingM))
                
                // Embedded presets layout
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Visual Presets",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Spacer(Modifier.height(Dimens.SpacingS))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val presets = listOf(
                            "pills" to "Pills",
                            "liquid" to "Syrup",
                            "cream" to "Cream",
                            "surgical" to "Surg.",
                            "spray" to "Spray",
                            "general" to "Gen."
                        )
                        items(presets) { (presetKey, label) ->
                            val isSelected = imageUrl == "preset:$presetKey"
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Primary.copy(alpha = 0.12f) else Color.Transparent)
                                    .border(1.dp, if (isSelected) Primary else Border, RoundedCornerShape(8.dp))
                                    .clickable { imageUrl = "preset:$presetKey" }
                                    .padding(horizontal = Dimens.SpacingS, vertical = 6.dp)
                            ) {
                                ProductImage(
                                    imageUrl = "preset:$presetKey",
                                    contentDescription = label,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Primary else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(Dimens.SpacingS))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Choose custom photo...",
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── Card 1: Product Information ─────────────────────────────────
            ChemistCard(contentPadding = Dimens.CardPadding) {
                CardSectionHeader(title = "Product Details", icon = Icons.Outlined.Inventory2)
                
                ChemistTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Item Name *",
                    placeholder = "e.g., Paracetamol 500mg",
                    isError = nameError,
                    errorMessage = if (nameError) "Item name is required" else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
                )

                Spacer(Modifier.height(Dimens.SpacingL))

                ChemistDropdown(
                    selectedValue = category,
                    onValueChange = { category = it },
                    options = categories,
                    label = "Category *",
                    isError = categoryError,
                    errorMessage = if (categoryError) "Select a category" else null
                )

                Spacer(Modifier.height(Dimens.SpacingL))

                ChemistTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = "Batch Number",
                    placeholder = "e.g., BTH-2025-001",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
                )
            }

            // ── Card 2: Pricing & Profitability ─────────────────────────────
            ChemistCard(contentPadding = Dimens.CardPadding) {
                CardSectionHeader(title = "Pricing & Margins", icon = Icons.Outlined.MonetizationOn)
                
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
                    ChemistTextField(
                        value = buyingPrice,
                        onValueChange = { buyingPrice = it },
                        label = "Buying Price (KES)",
                        placeholder = "0.00",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                        isError = buyingPriceError,
                        errorMessage = if (buyingPriceError) "Enter a valid price" else null,
                        modifier = Modifier.weight(1f)
                    )
                    ChemistTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        label = "Selling Price (KES) *",
                        placeholder = "0.00",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                        isError = sellingPriceError,
                        errorMessage = if (sellingPriceError) "Must be > 0" else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dynamic Profit Margin calculation block
                if (buyVal > 0.0 && sellVal > 0.0) {
                    Spacer(Modifier.height(Dimens.SpacingM))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (profitVal >= 0) SuccessTint else DangerTint)
                            .border(1.dp, if (profitVal >= 0) Success.copy(alpha = 0.2f) else Danger.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingS),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (profitVal >= 0) "Profit Margin / markup" else "Selling at Loss!",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (profitVal >= 0) Success else Danger
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "KES ${String.format("%.2f", profitVal)} / unit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (profitVal >= 0) Success else Danger
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Markup: ${String.format("%.1f", markupPercent)}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (profitVal >= 0) Success else Danger
                            )
                            Text(
                                text = "Margin: ${String.format("%.1f", marginPercent)}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (profitVal >= 0) Success.copy(alpha = 0.7f) else Danger.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // ── Card 3: Stock Management & Expiry ───────────────────────────
            ChemistCard(contentPadding = Dimens.CardPadding) {
                CardSectionHeader(title = "Inventory & Stock", icon = Icons.Outlined.BarChart)
                
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM)) {
                    ChemistTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = "Quantity",
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                        isError = quantityError,
                        errorMessage = if (quantityError) "Invalid quantity" else null,
                        modifier = Modifier.weight(1f)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        ChemistTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = "Unit",
                            placeholder = "pcs, tabs, ml",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
                        )
                    }
                }

                // Quick Suggestion Chips for Unit
                Spacer(Modifier.height(Dimens.SpacingS))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val suggestionUnits = listOf("pcs", "tabs", "caps", "ml", "vials", "bottles", "box")
                    items(suggestionUnits) { suggestion ->
                        val isSelected = unit.lowercase() == suggestion
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Primary.copy(alpha = 0.12f) else PageBackground)
                                .border(1.dp, if (isSelected) Primary else Border, RoundedCornerShape(8.dp))
                                .clickable { unit = suggestion }
                                .padding(horizontal = Dimens.SpacingM, vertical = 6.dp)
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Primary else TextSecondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.SpacingL))

                ChemistTextField(
                    value = reorderLevel,
                    onValueChange = { reorderLevel = it },
                    label = "Low Stock Alert Level",
                    placeholder = "10",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                Spacer(Modifier.height(Dimens.SpacingL))

                ChemistDatePicker(
                    selectedDate = expiryDate,
                    onDateSelected = { expiryDate = it },
                    label = "Expiry Date",
                    placeholder = "Select expiry date"
                )

                // Quick-Select Date Shortcut Chips
                Spacer(Modifier.height(Dimens.SpacingS))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
                ) {
                    listOf(
                        "+6M" to 6L,
                        "+1Y" to 12L,
                        "+2Y" to 24L
                    ).forEach { (label, months) ->
                        val shortcutDate = LocalDate.now().plusMonths(months)
                        val isSelected = expiryDate == shortcutDate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Primary.copy(alpha = 0.12f) else Color.Transparent)
                                .border(1.dp, if (isSelected) Primary else Border, RoundedCornerShape(8.dp))
                                .clickable { expiryDate = shortcutDate }
                                .padding(vertical = Dimens.SpacingS),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Primary else TextSecondary
                            )
                        }
                    }
                    
                    if (expiryDate != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Transparent)
                                .border(1.dp, Danger.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { expiryDate = null }
                                .padding(vertical = Dimens.SpacingS),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Clear",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Danger
                            )
                        }
                    }
                }
            }

            // ── Card 4: Compliance ──────────────────────────────────────────
            ChemistCard(contentPadding = Dimens.CardPadding) {
                CardSectionHeader(title = "Compliance", icon = Icons.Outlined.Shield)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Requires Prescription", style = MaterialTheme.typography.titleSmall)
                        Text("Flag item as Rx only (requires prescription check)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = requiresPrescription,
                        onCheckedChange = { requiresPrescription = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Surface,
                            checkedTrackColor = Primary
                        )
                    )
                }
            }

            // ── Save Button ─────────────────────────────────────────────────
            Spacer(Modifier.height(Dimens.SpacingM))

            ChemistPrimaryButton(
                text = if (isEditing) "Update Item" else "Save Item",
                onClick = {
                    hasAttemptedSave = true
                    if (isFormValid) {
                        val baseItem = item ?: InventoryItem(id = java.util.UUID.randomUUID().toString())
                        val savedItem = baseItem.copy(
                            name = name.trim(),
                            category = category,
                            batchNumber = batchNumber.trim(),
                            buyingPrice = buyingPrice.toDoubleOrNull() ?: 0.0,
                            sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                            quantity = quantity.toIntOrNull() ?: 0,
                            unit = unit.trim().ifBlank { "pcs" },
                            reorderLevel = reorderLevel.toIntOrNull() ?: 10,
                            expiryDate = expiryDate,
                            requiresPrescription = requiresPrescription,
                            imageUrl = imageUrl
                        )
                        onSave(savedItem)
                        onDismiss()
                    }
                },
                enabled = true
            )

            if (isEditing && onDelete != null) {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onDelete(item!!)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Delete Item",
                        color = Danger,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingXxl))
        }
    }
}

@Composable
private fun CardSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color = Primary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = Dimens.SpacingM)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Dimens.SpacingS))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
