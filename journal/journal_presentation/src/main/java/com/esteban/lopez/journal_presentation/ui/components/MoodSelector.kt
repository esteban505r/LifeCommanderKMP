package com.esteban.ruano.journal_presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import services.dailyjournal.models.MoodType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoodSelector(
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Positive", "Neutral", "Negative", "Physical", "Complex")
    
    Column(modifier = modifier) {
        // Category Filter
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                CategoryChip(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { selectedCategory = category }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Mood Grid
        val filteredMoods = if (selectedCategory == "All") {
            MoodType.values().toList()
        } else {
            MoodType.values().filter { it.category == selectedCategory }
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(300.dp)
        ) {
            items(filteredMoods) { mood ->
                MoodOption(
                    mood = mood,
                    isSelected = selectedMood == mood,
                    onClick = { onMoodSelected(mood) }
                )
            }
        }
        
        // Selected Mood Display
        selectedMood?.let { mood ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                backgroundColor = getMoodCategoryColor(mood.category).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mood.emoji,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = mood.label,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = mood.category,
                            style = MaterialTheme.typography.caption,
                            color = getMoodCategoryColor(mood.category)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        getMoodCategoryColor(category)
    } else {
        MaterialTheme.colors.surface
    }
    
    val textColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colors.onSurface
    }
    
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .shadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        color = backgroundColor,
        border = if (!isSelected) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            )
        } else null
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.body2,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
fun MoodOption(
    mood: MoodType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = tween(durationMillis = 200)
    )
    
    Box(
        modifier = modifier
            .size(70.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(elevation, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) {
                    getMoodCategoryColor(mood.category)
                } else {
                    MaterialTheme.colors.surface
                }
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) {
                    getMoodCategoryColor(mood.category)
                } else {
                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = mood.emoji,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mood.label,
                style = MaterialTheme.typography.caption,
                textAlign = TextAlign.Center,
                fontSize = 9.sp,
                maxLines = 2,
                color = if (isSelected) Color.White else MaterialTheme.colors.onSurface
            )
        }
    }
}

@Composable
fun getMoodCategoryColor(category: String): Color {
    return when (category) {
        "Positive" -> Color(0xFF4CAF50) // Green
        "Neutral" -> Color(0xFF9E9E9E)  // Grey
        "Negative" -> Color(0xFFF44336) // Red
        "Physical" -> Color(0xFFFF9800) // Orange
        "Complex" -> Color(0xFF9C27B0)  // Purple
        else -> MaterialTheme.colors.primary
    }
}

