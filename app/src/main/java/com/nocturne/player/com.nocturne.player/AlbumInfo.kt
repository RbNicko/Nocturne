package com.nocturne.player.data

data class AlbumInfo(
    val title: String,
    val artist: String,
    val trackCount: Int,
    val year: Int?,
    val tracks: List<AudioTrack>
)
