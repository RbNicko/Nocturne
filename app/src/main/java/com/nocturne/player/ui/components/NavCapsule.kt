package com.nocturne.player.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.nocturne.player.PlaybackUiState
import com.nocturne.player.ui.theme.*

/**
 * Controla cuánto está oculta la cápsula de navegación.
 *
 * Escucha el scroll de cualquier lista que esté dentro del contenedor
 * donde se aplique [connection]: al bajar por la lista la cápsula se va
 * escondiendo poco a poco, y al subir vuelve a su lugar. Si el usuario
 * suelta el dedo a medias, la cápsula termina de esconderse (o de salir)
 * con una animación suave.
 */
@Stable
class CapsuleScrollState {

    // Cuántos píxeles lleva desplazada hacia abajo (0 = visible por completo).
    var hiddenPx by mutableFloatStateOf(0f)
        private set

    // Alto total de la cápsula, incluyendo sus márgenes. Lo informa la propia cápsula.
    var heightPx by mutableFloatStateOf(0f)

    val connection = object : NestedScrollConnection {

        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            if (heightPx > 0f) {
                // available.y < 0 significa que se baja por la lista.
                hiddenPx = (hiddenPx - available.y).coerceIn(0f, heightPx)
            }

            // No consumimos nada: la lista se mueve con normalidad.
            return Offset.Zero
        }

        override suspend fun onPostFling(
            consumed: Velocity,
            available: Velocity
        ): Velocity {
            settle()
            return Velocity.Zero
        }
    }

    // Termina de esconder o de mostrar la cápsula, según lo que esté más cerca.
    suspend fun settle() {
        if (heightPx <= 0f) return

        val target = if (hiddenPx > heightPx / 2f) heightPx else 0f

        if (hiddenPx != target) {
            animate(hiddenPx, target) { value, _ ->
                hiddenPx = value
            }
        }
    }

    // Vuelve a mostrar la cápsula con una animación.
    suspend fun show() {
        if (hiddenPx != 0f) {
            animate(hiddenPx, 0f) { value, _ ->
                hiddenPx = value
            }
        }
    }
}

@Composable
fun rememberCapsuleScrollState(): CapsuleScrollState =
    remember { CapsuleScrollState() }

private val tabIcons: List<ImageVector> = listOf(
    Icons.Filled.Home,
    Icons.Filled.LibraryMusic,
    Icons.Filled.Settings
)

private val tabLabels = listOf(
    "Inicio",
    "Biblioteca",
    "Ajustes"
)

/**
 * Barra inferior flotante.
 *
 * - En Inicio: el icono de "Inicio" se reemplaza por la portada de la
 *   canción actual, con un contorno que se llena según avanza.
 * - En el resto de pantallas: aparece encima de la cápsula el mini
 *   reproductor alargado (portada, título, artista y pausa).
 *
 * Todo el conjunto se oculta junto con el scroll.
 */
@Composable
fun NavCapsule(
    selectedTab: Int,
    onTabClick: (Int) -> Unit,
    scrollState: CapsuleScrollState,
    playback: PlaybackUiState,
    positionMs: () -> Long,
    onTogglePlayPause: () -> Unit,
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {

    val showBar = playback.hasMedia && selectedTab != 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            // Debe ir antes de los márgenes para medir el conjunto completo.
            .onSizeChanged { size ->
                scrollState.heightPx = size.height.toFloat()
            }
            // Se lee dentro del bloque para no recomponer en cada frame.
            .graphicsLayer {
                translationY = scrollState.hiddenPx
            }
            .navigationBarsPadding()
            .padding(
                top = 8.dp,
                bottom = 14.dp
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AnimatedVisibility(
                visible = showBar,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    MiniPlayerBar(
                        playback = playback,
                        onTogglePlayPause = onTogglePlayPause,
                        onClick = onOpenPlayer,
                        modifier = Modifier.padding(horizontal = 22.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Row(
                modifier = Modifier
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                tabIcons.forEachIndexed { index, icon ->

                    val selected = selectedTab == index

                    // En Inicio, con música cargada, la pestaña de inicio
                    // muestra la portada en lugar del icono.
                    val showOrb =
                        index == 0 && selectedTab == 0 && playback.hasMedia

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected && !showOrb) {
                                    Color(0xFF292034)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .clickable {
                                if (showOrb) {
                                    onOpenPlayer()
                                } else {
                                    onTabClick(index)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {

                        if (showOrb) {
                            NowPlayingOrb(
                                artworkUri = playback.artworkUri,
                                isPlaying = playback.isPlaying,
                                progress = {
                                    if (playback.durationMs > 0L) {
                                        positionMs().toFloat() /
                                            playback.durationMs
                                    } else {
                                        0f
                                    }
                                },
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = tabLabels[index],
                                tint = if (selected) Accent else TextSecondary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}