package com.habit.mychemistpos.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import com.habit.mychemistpos.ui.theme.Border
import com.habit.mychemistpos.ui.theme.Dimens
import com.habit.mychemistpos.ui.theme.Primary
import com.habit.mychemistpos.ui.theme.Surface
import com.habit.mychemistpos.ui.theme.TextHint

@Composable
fun ChemistBottomNavBar(
    currentRoute: String?,
    onItemSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HorizontalDivider(thickness = Dimens.DividerThickness, color = Border)
        NavigationBar(
            containerColor = Surface,
            tonalElevation = 0.dp,
        ) {
            BottomNavItem.entries.forEach { item ->
                val selected = currentRoute == item.screen.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onItemSelected(item.screen) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(Dimens.IconSize)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        unselectedIconColor = TextHint,
                        unselectedTextColor = TextHint,
                        indicatorColor = Surface,
                    )
                )
            }
        }
    }
}
