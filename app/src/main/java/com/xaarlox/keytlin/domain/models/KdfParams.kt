package com.xaarlox.keytlin.domain.models

data class KdfParams(
    val memoryKib: Int = 65_536,
    val iterations: Int = 3,
    val parallelism: Int = 1
)