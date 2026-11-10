package com.xaarlox.keytlin.domain.service

import com.xaarlox.keytlin.domain.models.Vault
import com.xaarlox.keytlin.domain.models.VaultEntry

/**
 * Merges two versions of the vault (local and server) entry by entry
 *
 * The server does not see the content (zero-knowledge), so the client resolves conflicts:
 * - entry exists in only one version → taken as is;
 * - entry exists in both → the one modified later (modifiedAt) wins;
 * - if timestamps are equal, deletion (tombstone) takes precedence over modification,
 *   and in case of a complete match, the local version is taken
 *
 * The operation is deterministic and idempotent: merge(a, merge(a, b)) == merge(a, b)
 */
class VaultMerger {

    fun merge(local: Vault, remote: Vault): Vault {
        val remoteById = remote.entries.associateBy { it.id }
        val localIds = HashSet<String>(local.entries.size)

        val merged = local.entries.map { localEntry ->
            localIds += localEntry.id
            val remoteEntry = remoteById[localEntry.id]
            if (remoteEntry == null) localEntry else pick(localEntry, remoteEntry)
        }
        val onlyRemote = remote.entries.filter { it.id !in localIds }

        return Vault(entries = merged + onlyRemote)
    }

    private fun pick(local: VaultEntry, remote: VaultEntry): VaultEntry = when {
        remote.modifiedAt > local.modifiedAt -> remote
        remote.modifiedAt < local.modifiedAt -> local
        remote.isDeleted && !local.isDeleted -> remote
        else -> local
    }
}