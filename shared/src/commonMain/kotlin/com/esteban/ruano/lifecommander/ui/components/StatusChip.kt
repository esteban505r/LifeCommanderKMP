package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.ExerciseSet

@Composable
fun StatusChip(text: String, tint: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(tint.copy(.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .background(tint, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.caption.copy(color = tint, fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
fun StatChip(value: String, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colors.onSurface.copy(.06f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.overline.copy(color = MaterialTheme.colors.onSurface.copy(.6f))
        )
    }
}

@Composable
fun BannerInfo(text: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colors.onSurface.copy(.05f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colors.onSurface.copy(.7f))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(.8f)))
    }
}

@Composable
fun BannerError(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colors.error.copy(.10f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colors.error)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.error))
    }
}

@Composable
fun ActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    colors: ButtonColors,
    border: BorderStroke? = null,
) {
    Button(
        onClick = onClick,
        colors = colors,
        border = border,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        modifier = Modifier
            .defaultMinSize(minHeight = 40.dp, minWidth = 96.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun SetRow(
    index: Int,
    set: ExerciseSet,
    editableSetId: String?,
    canMutate: Boolean,
    onEditToggle: () -> Unit,
    onSave: (newReps: Int) -> Unit,
    onRemove: () -> Unit,
    focusBump: Int,
) {
    val isEditing = editableSetId == set.id
    var editText by remember(set.id) { mutableStateOf(set.reps.toString()) }

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(isEditing, focusBump) {
        if (isEditing) {
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isEditing) MaterialTheme.colors.primary.copy(.10f)
                else MaterialTheme.colors.onSurface.copy(.04f)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Set ${index + 1}",
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Medium)
        )
        Spacer(Modifier.width(10.dp))

        if (isEditing && canMutate) {
            OutlinedTextField(
                value = editText,
                onValueChange = { v -> editText = v.filter(Char::isDigit).take(4) },
                singleLine = true,
                modifier = Modifier
                    .width(96.dp)
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.body2,
                label = { Text("Reps") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                editText.toIntOrNull()?.let(onSave)
            }) {
                Icon(Icons.Filled.Check, contentDescription = "Save")
            }
            IconButton(onClick = onEditToggle) {
                Icon(Icons.Filled.Close, contentDescription = "Cancel")
            }
        } else {
            Text(
                "${set.reps} reps",
                style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(.85f))
            )
            Spacer(Modifier.weight(1f))
            if (canMutate) {
                IconButton(onClick = onEditToggle) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = MaterialTheme.colors.error)
                }
            } else {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colors.primary)
            }
        }
    }
}
/* ----------------------------- UI atoms ----------------------------- */


@Composable
fun RowScope.ActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    colors: ButtonColors,
    border: BorderStroke? = null,
) {
    Button(
        onClick = onClick,
        colors = colors,
        border = border,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        modifier = Modifier.weight(1f)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        /*Spacer(Modifier.width(8.dp))
      *//*  Text(
            label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )*/
    }
}