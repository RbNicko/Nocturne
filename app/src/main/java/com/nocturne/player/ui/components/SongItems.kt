package com.nocturne.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.nocturne.player.ui.theme.*

@Composable
    fun SongItem(
        title: String,
        artist: String,
        symbol: String,
        onClick: () -> Unit = {}
    ) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    SurfaceDark,
                    RoundedCornerShape(13.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = symbol,
                color = Accent,
                fontSize = 25.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = artist,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Text(
            text = "•••",
            color = TextSecondary,
            fontSize = 19.sp
        )
    }
}
