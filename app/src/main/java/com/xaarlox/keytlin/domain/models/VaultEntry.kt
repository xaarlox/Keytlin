package com.xaarlox.keytlin.domain.models

data class VaultEntry(
    val id: String,
    val title: String,
    val username: String,
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)