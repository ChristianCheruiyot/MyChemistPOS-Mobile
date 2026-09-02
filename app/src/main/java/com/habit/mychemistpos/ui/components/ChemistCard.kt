package com.habit.mychemistpos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.habit.mychemistpos.ui.theme.Border
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.Surface

@Composable
fun ChemistCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: androidx.compose.ui.unit.Dp = Dimens.CardPadding,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Smooth micro-interaction scaling for click feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        label = "CardScale"
    )

    val cardModifier = modifier
        .fillMaxWidth()
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick
                )
            } else Modifier
        )

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (onClick != null) 1.dp else 0.dp,
            pressedElevation = 0.5.dp
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(Dimens.CardCorner),
        border = BorderStroke(Dimens.BorderWidth, Border.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
