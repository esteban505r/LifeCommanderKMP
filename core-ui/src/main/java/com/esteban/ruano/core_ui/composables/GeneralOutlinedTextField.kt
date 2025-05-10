package com.esteban.ruano.core_ui.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.theme.Gray
import com.esteban.ruano.core_ui.theme.Gray2
import com.esteban.ruano.core_ui.R

@Composable
fun GeneralOutlinedTextField(value: String,placeHolder: String? = null, onValueChange: (String) -> Unit){
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = Color.White,
            focusedBorderColor = Gray2,
            unfocusedBorderColor = Gray2,
            placeholderColor = Gray
        ),
        value = value ,
        onValueChange = onValueChange,
        label = if(placeHolder!=null) { {Text(placeHolder)} } else null
    )
}

@Composable
fun GeneralOutlinedTextField(value: String,placeHolder: Int? = null, onValueChange: (String) -> Unit){
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = Color.White,
            focusedBorderColor = Gray2,
            unfocusedBorderColor = Gray2,
            placeholderColor = Gray
        ),
        value = value ,
        onValueChange = onValueChange,
        label =  if(placeHolder!=null) { { Text(stringResource(id = placeHolder)) } } else null
    )
}

@Composable
fun GeneralOutlinedTextField(value: String, onValueChange: (String) -> Unit){
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = Color.White,
            focusedBorderColor = Gray2,
            unfocusedBorderColor = Gray2,
            placeholderColor = Gray
        ),
        value = value ,
        onValueChange = onValueChange
    )
}