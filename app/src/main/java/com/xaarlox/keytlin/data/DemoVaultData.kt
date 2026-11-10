package com.xaarlox.keytlin.data

import com.xaarlox.keytlin.domain.models.Vault
import com.xaarlox.keytlin.domain.models.VaultEntry

/**
 * A demo vault that the application starts with until persistent storage is available
 */
object DemoVaultData {
    const val MASTER_PASSWORD = "1503"

    val vault = Vault(
        entries = listOf(
            VaultEntry(
                id = "1",
                title = "Google",
                username = "sarah.connor@gmail.com",
                password = "Tr0ub4dor&3xK",
                url = "https://accounts.google.com"
            ),
            VaultEntry(
                id = "2",
                title = "GitHub",
                username = "git-connor",
                password = "password",
                url = "https://github.com"
            ),
            VaultEntry(
                id = "3",
                title = "Netflix",
                username = "connor.family@netflix.com",
                password = "n3tfl1x-Family"
            ),
            VaultEntry(
                id = "4",
                title = "Slack Workspace",
                username = "sarah@cyberdyne.io",
                password = "Sk\$net!2029-judgement"
            ),
            VaultEntry(
                id = "5",
                title = "Amazon Web Services",
                username = "aws-admin",
                password = "qwerty123"
            )
        )
    )
}