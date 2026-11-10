package com.xaarlox.keytlin.data.local

import com.xaarlox.keytlin.domain.models.Vault

interface VaultLocalDataSource {
    suspend fun hasVault(): Boolean
    suspend fun create(masterPassword: String, vault: Vault)
    suspend fun read(masterPassword: String): Vault?
    suspend fun write(vault: Vault)
    suspend fun clear()
}