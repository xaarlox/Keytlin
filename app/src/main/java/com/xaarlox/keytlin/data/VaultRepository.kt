package com.xaarlox.keytlin.data

interface VaultRepository {
    fun validateSyncCode(code: String): Boolean
    fun validateMasterPassword(password: String): Boolean
}

class FakeVaultRepository : VaultRepository {
    override fun validateSyncCode(code: String): Boolean {
        return code == "1234"
    }

    override fun validateMasterPassword(password: String): Boolean {
        return password == "1503"
    }
}