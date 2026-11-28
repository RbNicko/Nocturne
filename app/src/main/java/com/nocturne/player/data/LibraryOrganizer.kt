package com.nocturne.player.data

object LibraryOrganizer {

    private val featuringSeparator = Regex(
        "(?i)\\s+(?:feat\\.?|ft\\.?|featuring)\\s+"
    )

    private val collaborationSeparator = Regex(
        "(?i)\\s+(?:feat\\.?|ft\\.?|featuring)\\s+|\\s*&\\s*|\\s+x\\s*|,\\s*"
    )

    fun getMainArtist(track: AudioTrack): String {

        return track.albumArtist
            ?.takeIf { it.isNotBlank() }
            ?: featuringSeparator
                .split(track.artist)
                .first()
                .trim()
    }

    private fun getFeaturedArtists(track: AudioTrack): List<String> {

        val albumArtist = track.albumArtist
            ?.takeIf { it.isNotBlank() }

        val separator = if (albumArtist != null) {
            collaborationSeparator
        } else {
            featuringSeparator
        }

        // Se calcula una sola vez (antes se recalculaba por cada elemento).
        val mainArtist = getMainArtist(track)

        return separator
            .split(track.artist)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.equals(mainArtist, ignoreCase = true) }
            .distinctBy { it.lowercase() }
    }

    // Versión corta: calcula los álbumes por su cuenta.
    fun getArtists(tracks: List<AudioTrack>): List<ArtistInfo> =
        getArtists(tracks, getAlbums(tracks))

    // Versión que reutiliza los álbumes ya calculados.
    fun getArtists(
        tracks: List<AudioTrack>,
        albums: List<AlbumInfo>
    ): List<ArtistInfo> {

        // Artistas invitados de cada álbum (en minúsculas), calculados una
        // sola vez. Antes se repetía el trabajo de separar nombres con
        // expresiones regulares por cada combinación artista × álbum.
        val featuredByAlbum: List<Set<String>> = albums.map { album ->
            album.tracks
                .flatMap { getFeaturedArtists(it) }
                .map { it.lowercase() }
                .toSet()
        }

        val ownAlbumsByArtist = albums.groupBy { it.artist.lowercase() }

        val mainArtists = tracks.groupBy {
            getMainArtist(it)
        }

        return mainArtists.map { (artistName, artistTracks) ->

            val key = artistName.lowercase()

            val ownAlbums = ownAlbumsByArtist[key].orEmpty()

            val featuredAlbums = albums.filterIndexed { index, album ->
                !album.artist.equals(
                    artistName,
                    ignoreCase = true
                ) && key in featuredByAlbum[index]
            }

            ArtistInfo(
                name = artistName,
                albumCount = ownAlbums.size,
                trackCount = artistTracks.size,
                albums = ownAlbums.map { it.title },
                featuredAlbums = featuredAlbums
            )
        }.sortedBy {
            it.name.lowercase()
        }
    }

    fun getAlbums(tracks: List<AudioTrack>): List<AlbumInfo> {

        return tracks
            .groupBy { track ->
                getMainArtist(track) to track.album
            }
            .map { (key, albumTracks) ->

                val (artistName, albumName) = key

                AlbumInfo(
                    title = albumName,
                    artist = artistName,
                    trackCount = albumTracks.size,
                    year = albumTracks
                        .mapNotNull { it.year }
                        .minOrNull(),
                    tracks = albumTracks.sortedWith(
                        compareBy<AudioTrack> {
                            it.discNumber ?: Int.MAX_VALUE
                        }.thenBy {
                            it.trackNumber ?: Int.MAX_VALUE
                        }.thenBy {
                            it.title.lowercase()
                        }
                    )
                )
            }
            .sortedWith(
                compareBy<AlbumInfo> {
                    it.title.lowercase()
                }.thenBy {
                    it.artist.lowercase()
                }
            )
    }
}