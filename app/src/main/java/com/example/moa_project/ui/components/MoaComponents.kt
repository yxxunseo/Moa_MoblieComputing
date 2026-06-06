package com.example.moa_project.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.theme.MoaAccentBlueBg
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaButtonSpec
import com.example.moa_project.ui.theme.MoaInputBackground
import com.example.moa_project.ui.theme.MoaInputBorder
import com.example.moa_project.ui.theme.MoaPlaceholder
import com.example.moa_project.ui.theme.MoaRadius
import com.example.moa_project.ui.theme.MoaTextPrimary
import com.example.moa_project.ui.theme.MoaTextSecondary
import com.example.moa_project.ui.theme.SBAggroFontFamily

@Composable
fun MoaOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLength: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = MoaTextSecondary,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (maxLength == null || newValue.length <= maxLength) onValueChange(newValue)
            },
            placeholder = if (placeholder.isNotEmpty()) {
                {
                    Text(
                        text = placeholder,
                        color = MoaPlaceholder,
                        fontFamily = SBAggroFontFamily,
                        fontSize = 14.sp,
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(MoaRadius.button),
            trailingIcon = if (maxLength != null) {
                {
                    Text(
                        text = "${value.length}/$maxLength",
                        color = MoaPlaceholder,
                        fontFamily = SBAggroFontFamily,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MoaBlue,
                unfocusedBorderColor = MoaInputBorder,
                focusedTextColor = MoaTextPrimary,
                unfocusedTextColor = MoaTextPrimary,
                cursorColor = MoaBlue,
                focusedContainerColor = MoaInputBackground,
                unfocusedContainerColor = MoaInputBackground,
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = SBAggroFontFamily,
                fontSize = 14.sp,
                color = MoaTextPrimary,
            ),
        )
    }
}

@Composable
fun MoaDialogLabel(text: String) {
    Text(
        text = text,
        fontFamily = SBAggroFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        color = MoaTextSecondary,
    )
}

@Composable
fun MoaDialogButtonText(text: String, color: Color = MoaBlue) {
    Text(
        text = text,
        fontFamily = SBAggroFontFamily,
        fontWeight = FontWeight.Bold,
        color = color,
    )
}

@Composable
fun MoaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = MoaButtonSpec.height,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(MoaRadius.button),
        colors = ButtonDefaults.buttonColors(containerColor = MoaBlue),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                fontFamily = SBAggroFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
fun MoaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = MoaButtonSpec.height,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(MoaRadius.button),
        colors = ButtonDefaults.buttonColors(containerColor = MoaAccentBlueBg),
    ) {
        Text(
            text = text,
            color = MoaBlue,
            fontFamily = SBAggroFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}
