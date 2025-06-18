package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PlaceholderScreen(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.Construction,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Card(
            modifier = Modifier.size(120.dp),
            backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
            elevation = 0.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colors.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.body1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Coming Soon Badge
        Card(
            backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f),
            elevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colors.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Coming Soon",
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colors.secondary
                )
            }
        }
    }
}

@Composable
fun LoadingScreen(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colors.primary,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error Icon
        Card(
            modifier = Modifier.size(120.dp),
            backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
            elevation = 0.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colors.error
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Error Message
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.body1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
} 