package com.xaarlox.keytlin.domain.service

import com.xaarlox.keytlin.domain.models.Vault
import kotlinx.serialization.json.Json

class VaultSerializer {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun toBytes(vault: Vault): ByteArray =
        json.encodeToString(vault).toByteArray(Charsets.UTF_8)

    fun fromBytes(bytes: ByteArray): Vault =
        json.decodeFromString(bytes.toString(Charsets.UTF_8))
}