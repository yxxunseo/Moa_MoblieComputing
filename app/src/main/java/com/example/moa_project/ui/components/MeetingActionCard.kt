package com.example.moa_project.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moa_project.ui.theme.MoaBlue
import com.example.moa_project.ui.theme.MoaRadius
import com.example.moa_project.ui.theme.MoaTextPrimary

@Composable
fun MeetingActionCard(
    titlePrefix: String,
    titleSuffix: String,
    description: String,
    icon: @Composable () -> Unit,
    mascotSize: androidx.compose.ui.unit.Dp = 72.dp,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .shadow(4.dp, RoundedCornerShape(MoaRadius.card), spotColor = Color(0x14000000))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(MoaRadius.card),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MoaMascot(size = mascotSize)
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                MoaTitleText(
                    text = titlePrefix + titleSuffix,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MoaTextPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                MoaCaptionText(text = description)
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    icon()
                }
            }
        }
    }
}
