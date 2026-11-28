package com.nocturne.player.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocturne.player.ui.theme.*

@Composable
fun ExcludedFoldersScreen(
    folders: List<String>,
    onBack: () -> Unit,
    // Recibe la carpeta elegida. Devuelve false si no se pudo usar.
    onFolderPicked: (Uri) -> Boolean,
    onRemoveFolder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null && !onFolderPicked(uri)) {
            Toast.makeText(
                context,
                "Elige una carpeta, no el almacenamiento completo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 28.dp
        )
    ) {

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Carpetas excluidas",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Las canciones de estas carpetas no aparecerán en tu biblioteca.",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { folderPicker.launch(null) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Accent
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Agregar carpeta",
                    color = Accent
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (folders.isEmpty()) {

            item {
                Text(
                    text = "No hay carpetas excluidas.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }

        } else {

            items(
                items = folders,
                key = { it }
            ) { folder ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = folder.trimEnd('/'),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { onRemoveFolder(folder) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Quitar carpeta",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }
    }
}