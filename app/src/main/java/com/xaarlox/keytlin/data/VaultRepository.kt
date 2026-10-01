package com.xaarlox.keytlin.data

interface VaultRepository {
    fun validateSyncCode(code: String): Boolean
}

class FakeVaultRepository : VaultRepository {
    override fun validateSyncCode(code: String): Boolean {
        return code == "1234"
    }
}