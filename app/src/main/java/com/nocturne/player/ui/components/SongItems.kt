package com.nocturne.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocturne.player.ui.theme.*

@Composable
fun SongItem(
    title: String,
    artist: String,
    symbol: String,
    artworkUri: Uri? = null,
    onClick: () -> Unit = {},
    format: String? = null,
    isFavorite: Boolean? = null,
    onFavoriteClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null
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
                .clip(RoundedCornerShape(13.dp))
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {

            // El símbolo se muestra si no hay portada o mientras carga.
            val symbolText: @Composable () -> Unit = {
                Text(
                    text = symbol,
                    color = Accent,
                    fontSize = 25.sp
                )
            }

            if (artworkUri != null) {
                AlbumArt(
                    artworkUri = artworkUri,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = symbolText
                )
            } else {
                symbolText()
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = artist,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (format != null) {
                Text(
                    text = format.uppercase(),
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }

        if (isFavorite != null && onFavoriteClick != null) {
            IconButton(
                onClick = onFavoriteClick
            ) {
                Icon(
                    imageVector = if (isFavorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Filled.FavoriteBorder
                    },
                    contentDescription = if (isFavorite) {
                        "Quitar de favoritos"
                    } else {
                        "Agregar a favoritos"
                    },
                    tint = if (isFavorite) Accent else TextSecondary
                )
            }
        }

        IconButton(
            onClick = { onMoreClick?.invoke() }
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Más opciones",
                tint = TextSecondary
            )
        }
    }
}