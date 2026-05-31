package com.example.moa_project.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaCardShadow
import com.example.moa_project.ui.theme.MoaInputBackground
import com.example.moa_project.ui.theme.MoaInputBorder
import com.example.moa_project.ui.theme.MoaPlaceholder
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
            onValueChange = onValueChange,
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
            shape = RoundedCornerShape(12.dp),
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

fun Modifier.moaCard(
    cornerRadius: Dp = 18.dp,
    shadowElevation: Dp = 8.dp,
): Modifier = this
    .shadow(shadowElevation, RoundedCornerShape(cornerRadius), spotColor = MoaCardShadow)
    .clip(RoundedCornerShape(cornerRadius))
