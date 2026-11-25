package com.nocturne.player.data

object LibraryOrganizer {

    private val featuringSeparator = Regex(
        "(?i)\\s+(?:feat\\.?|ft\\.?|featuring)\\s+"
    )

    private val collaborationSeparator = Regex(
        "(?i)\\s+(?:feat\\.?|ft\\.?|featuring)\\s+|\\s*&\\s*|\\s+x\\s*|,\\s*"
    )

    private fun getMainArtist(track: AudioTrack): String {

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

        return separator
            .split(track.artist)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot {
                it.equals(
                    getMainArtist(track),
                    ignoreCase = true
                )
            }
            .distinctBy { it.lowercase() }
    }

    fun getArtists(tracks: List<AudioTrack>): List<ArtistInfo> {

        val albums = getAlbums(tracks)

        val mainArtists = tracks.groupBy {
            getMainArtist(it)
        }

        return mainArtists.map { (artistName, artistTracks) ->

            val ownAlbums = albums.filter {
                it.artist.equals(artistName, ignoreCase = true)
            }

            val featuredAlbums = albums.filter { album ->
                !album.artist.equals(
                    artistName,
                    ignoreCase = true
                ) && album.tracks.any { track ->
                    getFeaturedArtists(track).any {
                        it.equals(artistName, ignoreCase = true)
                    }
                }
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
