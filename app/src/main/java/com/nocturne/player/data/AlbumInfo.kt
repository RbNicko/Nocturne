package com.nocturne.player.data

data class AlbumInfo(
    val title: String,
    val artist: String,
    val trackCount: Int,
    val year: Int?,
    val tracks: List<AudioTrack>
) {
    val genre: String?
        get() = tracks
            .mapNotNull { it.genre?.takeIf(String::isNotBlank) }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

    val audioFormat: String
        get() {
            val formats = tracks
                .mapNotNull { it.audioFormat?.takeIf(String::isNotBlank) }
                .distinct()

            return when {
                formats.isEmpty() -> "No disponible"
                formats.size == 1 -> formats.first()
                else -> "Varios formatos"
            }
        }

    val audioQuality: String
        get() {
            val qualities = tracks
                .mapNotNull { it.audioQuality?.takeIf(String::isNotBlank) }
                .distinct()

            return when {
                qualities.isEmpty() -> "No disponible"
                qualities.size > 1 -> "Mixta"
                tracks.any { it.audioQuality.isNullOrBlank() } -> "Parcial"
                else -> qualities.first()
            }
        }
}