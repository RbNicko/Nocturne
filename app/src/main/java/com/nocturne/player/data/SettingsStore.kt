package com.nocturne.player.data

import android.content.Context

/**
 * Guarda los ajustes del usuario en el teléfono.
 * Por ahora: las carpetas excluidas de la biblioteca.
 */
class SettingsStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        "nocturne_settings",
        Context.MODE_PRIVATE
    )

    /**
     * Rutas relativas de las carpetas excluidas, con "/" al final.
     * Ejemplo: "Music/Podcasts/"
     */
    fun getExcludedFolders(): Set<String> =
        prefs.getStringSet(KEY_EXCLUDED_FOLDERS, emptySet())
            ?.toSet()
            ?: emptySet()

    fun setExcludedFolders(folders: Set<String>) {
        prefs.edit()
            .putStringSet(KEY_EXCLUDED_FOLDERS, folders)
            .apply()
    }

    private companion object {
        const val KEY_EXCLUDED_FOLDERS = "excluded_folders"
    }
}