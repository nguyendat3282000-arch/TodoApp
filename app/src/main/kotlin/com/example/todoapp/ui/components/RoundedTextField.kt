// File: ui/components/RoundedTextField.kt
package com.example.todoapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import com.example.todoapp.ui.theme.Mint100
import com.example.todoapp.ui.theme.Mint500
import com.example.todoapp.ui.theme.TextFieldShape

/**
 * Reusable branded [OutlinedTextField] with rounded corners, Mint accent colors,
 * and consistent styling across all screens in the app.
 *
 * Extracted from [com.example.todoapp.ui.screens.auth.AuthScreen] so any screen
 * can import it without creating a dependency on another screen file.
 *
 * @param value                The current field value.
 * @param onValueChange        Callback invoked on every keystroke.
 * @param label                Label text displayed above the field.
 * @param modifier             Optional [Modifier] — defaults to [fillMaxWidth].
 * @param leadingIcon          Optional leading icon composable.
 * @param trailingIcon         Optional trailing icon composable (e.g. password toggle).
 * @param visualTransformation Password masking or custom transformation.
 * @param keyboardOptions      IME type and action configuration.
 * @param keyboardActions      Callbacks for IME actions (Next, Done, etc.).
 * @param enabled              Whether the field is interactive.
 * @param isError              Shows error styling when true.
 * @param supportingText       Optional supporting / error text below the field.
 */
@Composable
fun RoundedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value                = value,
        onValueChange        = onValueChange,
        label                = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon          = leadingIcon,
        trailingIcon         = trailingIcon,
        singleLine           = true,
        enabled              = enabled,
        isError              = isError,
        supportingText       = supportingText?.let { { Text(it) } },
        visualTransformation = visualTransformation,
        keyboardOptions      = keyboardOptions,
        keyboardActions      = keyboardActions,
        shape                = TextFieldShape,
        colors               = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = Mint500,
            unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
            focusedLabelColor       = Mint500,
            cursorColor             = Mint500,
            focusedContainerColor   = Mint100.copy(alpha = 0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
