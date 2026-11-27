package com.nocturne.player.data

data class ArtistInfo(
    val name: String,
    val albumCount: Int,
    val trackCount: Int,
    val albums: List<String>,

    // Álbumes de otros artistas donde participa como invitado
    val featuredAlbums: List<AlbumInfo> = emptyList()
)
