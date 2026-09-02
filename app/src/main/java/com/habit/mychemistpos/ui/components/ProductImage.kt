package com.habit.mychemistpos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun ProductImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFFE0F2F1)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null && imageUrl.startsWith("preset:")) {
            val preset = imageUrl.substringAfter("preset:")
            val (gradient, icon) = when (preset) {
                "pills" -> Pair(
                    Brush.horizontalGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4))),
                    Icons.Filled.Medication
                )
                "liquid" -> Pair(
                    Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B))),
                    Icons.Filled.Vaccines
                )
                "cream" -> Pair(
                    Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF6366F1))),
                    Icons.Filled.Healing
                )
                "surgical" -> Pair(
                    Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981))),
                    Icons.Filled.MedicalServices
                )
                "spray" -> Pair(
                    Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF3B82F6))),
                    Icons.Filled.Air
                )
                else -> Pair(
                    Brush.horizontalGradient(listOf(Color(0xFF0891B2), Color(0xFF0D9488))),
                    Icons.Filled.LocalPharmacy
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize(0.55f)
                )
            }
        } else if (!imageUrl.isNullOrBlank()) {
            coil3.compose.SubcomposeAsyncImage(
                model = android.net.Uri.parse(imageUrl),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(0.3f),
                            strokeWidth = 2.dp,
                            color = Color(0xFF00796B)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE0F2F1), Color(0xFFB2DFDB))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalPharmacy,
                            contentDescription = contentDescription,
                            tint = Color(0xFF00796B),
                            modifier = Modifier.fillMaxSize(0.5f)
                        )
                    }
                }
            )
        } else {
            // Default Fallback
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFE0F2F1), Color(0xFFB2DFDB))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalPharmacy,
                    contentDescription = contentDescription,
                    tint = Color(0xFF00796B),
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            }
        }
    }
}
