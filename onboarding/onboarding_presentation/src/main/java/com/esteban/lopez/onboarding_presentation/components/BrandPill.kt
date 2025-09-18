package com.esteban.lopez.onboarding_presentation.components



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BrandPill(name: String) {
    val initials = remember(name) {
        name.split(" ")
            .filter { it.isNotBlank() }
            .map { it.first().uppercaseChar() }
            .take(2)
            .joinToString("")
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFF6F7FB))
            .border(BorderStroke(1.dp, Color(0xFFE6EAF2)), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                initials,
                color = Color.White,
                style = MaterialTheme.typography.overline,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            name,
            style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colors.onSurface
        )
    }
}
