package com.esteban.ruano.onboarding_presentation.auth.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.button.BaseButton
import com.esteban.ruano.core_ui.theme.Gray
import com.esteban.ruano.core_ui.theme.Gray2
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import com.esteban.ruano.onboarding_presentation.auth.intent.AuthIntent
import com.esteban.ruano.onboarding_presentation.components.ActionButton

@Composable
fun LoginScreen(
    userIntent: (AuthIntent) -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val sendMainIntent = LocalMainIntent.current
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(
            24.dp
        ),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.login),
            style = MaterialTheme.typography.h2
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Gray2,
                unfocusedBorderColor = Gray2,
                placeholderColor = Gray
            ),
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.email)) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Gray2,
                unfocusedBorderColor = Gray2,
                placeholderColor = Gray
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = {passwordVisible = !passwordVisible}){
                    Icon(imageVector  = image, description)
                }
            },
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.password)) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        BaseButton(
            text = stringResource(id = R.string.login),
            onClick = {
                if(email.isNotEmpty() && password.isNotEmpty()) {
                    userIntent(AuthIntent.Login(email, password))
                }
                else{
                 sendMainIntent(
                     MainIntent.ShowSnackBar(
                         message = context.getString(R.string.error_empty_fields),
                            type = SnackbarType.ERROR
                     )
                 )
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
    }
}