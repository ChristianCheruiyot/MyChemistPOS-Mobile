package com.habit.mychemistpos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habit.mychemistpos.ui.theme.Border
import com.habit.mychemistpos.ui.theme.Dimens
import com.valentinilk.shimmer.shimmer

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = 16.dp,
) {
    Box(
        modifier = modifier
            .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(Border.copy(alpha = 0.6f))
    )
}

/** Shimmer skeleton that mimics a card with a few text lines */
@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    Box(modifier = modifier.shimmer()) {
        ChemistCard {
            ShimmerBox(height = 14.dp, width = 120.dp)
            Spacer(Modifier.height(Dimens.SpacingM))
            ShimmerBox(height = 12.dp)
            Spacer(Modifier.height(Dimens.SpacingS))
            ShimmerBox(height = 12.dp, width = 200.dp)
        }
    }
}

/** Shimmer skeleton row for list items */
@Composable
fun ShimmerListItem(modifier: Modifier = Modifier) {
    Box(modifier = modifier.shimmer()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(height = 14.dp, width = 140.dp)
                Spacer(Modifier.height(Dimens.SpacingS))
                ShimmerBox(height = 12.dp, width = 100.dp)
            }
            Spacer(Modifier.width(Dimens.SpacingL))
            ShimmerBox(height = 14.dp, width = 60.dp)
        }
    }
}

/** Multiple shimmer cards for loading state */
@Composable
fun ShimmerCardList(count: Int = 4) {
    Column(
        modifier = Modifier.padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing)
    ) {
        repeat(count) {
            ShimmerCard()
        }
    }
}
