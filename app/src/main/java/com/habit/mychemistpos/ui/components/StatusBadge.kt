package com.habit.mychemistpos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.habit.mychemistpos.ui.theme.Danger
import com.habit.mychemistpos.ui.theme.DangerTint
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.PrimaryTint
import com.habit.mychemistpos.ui.theme.Success
import com.habit.mychemistpos.ui.theme.SuccessTint
import com.habit.mychemistpos.ui.theme.Warning
import com.habit.mychemistpos.ui.theme.WarningTint

enum class BadgeType { INFO, SUCCESS, WARNING, DANGER }

@Composable
fun StatusBadge(
    text: String,
    type: BadgeType,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = when (type) {
        BadgeType.INFO -> PrimaryTint to Primary
        BadgeType.SUCCESS -> SuccessTint to Success
        BadgeType.WARNING -> WarningTint to Warning
        BadgeType.DANGER -> DangerTint to Danger
    }
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(Dimens.ChipCorner))
            .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingXs)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fg
        )
    }
}

/**
 * Returns a BadgeType based on stock quantity relative to reorder level.
 */
fun stockBadgeType(quantity: Int, reorderLevel: Int): BadgeType = when {
    quantity == 0 -> BadgeType.DANGER
    quantity <= reorderLevel -> BadgeType.WARNING
    else -> BadgeType.SUCCESS
}
