package com.nocturne.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocturne.player.data.RankedTrack
import com.nocturne.player.ui.theme.*
import java.util.Calendar

@Composable
fun HomeScreen(
    topTracks: List<RankedTrack>,
    selectedTopPeriod: Int,
    onTopPeriodChange: (Int) -> Unit,
    isTopLoading: Boolean,
    modifier: Modifier = Modifier
) {

    val currentHour = Calendar.getInstance()
        .get(Calendar.HOUR_OF_DAY)

    val greeting = when (currentHour) {
        in 5..11 -> "Buenos días."
        in 12..18 -> "Buenas tardes."
        else -> "Buenas noches."
    }

    val periods = listOf(
        "DÍA",
        "SEMANA",
        "MES",
        "AÑO",
        "TODO"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        contentPadding = PaddingValues(
            top = 28.dp,
            bottom = 28.dp
        )
    ) {

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NOCTURNE",
                        color = TextPrimary,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )

                    Text(
                        text = "Tu música. Tu espacio.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SurfaceDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "☾",
                        color = Accent,
                        fontSize = 25.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(42.dp))

            Text(
                text = greeting,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(34.dp))
        }

        item {
            Text(
                text = "RECIENTES",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(34.dp))
        }

        item {
            Text(
                text = "TOP 15",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                periods.forEachIndexed { index, period ->

                    val isSelected = selectedTopPeriod == index

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) SurfaceDark
                                else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable {
                                onTopPeriodChange(index)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period,
                            color = if (isSelected) Accent else TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isTopLoading -> {
                    Text(
                        text = "Cargando reproducciones...",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                topTracks.isEmpty() -> {
                    Text(
                        text = "Aún no hay reproducciones registradas.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                else -> {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val cardWidth = (maxWidth - 16.dp) / 3

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(topTracks) { index, rankedTrack ->

                                Column(
                                    modifier = Modifier
                                        .width(cardWidth)
                                        .height(176.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SurfaceDark)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        color = Accent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = rankedTrack.track.title,
                                        modifier = Modifier.height(36.dp),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(5.dp))

                                    Text(
                                        text = rankedTrack.track.artist,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "${rankedTrack.playCount} reproducciones",
                                        color = Accent,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(34.dp))
        }

        item {
            Text(
                text = "ÁLBUMES",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(34.dp))
        }

        item {
            Text(
                text = "ARTISTAS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(34.dp))
        }

        item {
            Text(
                text = "TIEMPO DE REPRODUCCIÓN",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}