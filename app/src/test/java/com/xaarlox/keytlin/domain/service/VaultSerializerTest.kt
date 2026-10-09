package com.xaarlox.keytlin.domain.service

import com.xaarlox.keytlin.domain.models.Vault
import com.xaarlox.keytlin.domain.models.VaultEntry
import junit.framework.TestCase.assertEquals
import org.junit.Test

class VaultSerializerTest {
    private val serializer = VaultSerializer()

    @Test
    fun roundTrip_returnsSameVault() {
        val vault = Vault(
            entries = listOf(
                VaultEntry(id = "1", title = "Google", username = "a@b.c", password = "p@ss")
            )
        )

        val restored = serializer.fromBytes(serializer.toBytes(vault))

        assertEquals(vault, restored)
    }
}