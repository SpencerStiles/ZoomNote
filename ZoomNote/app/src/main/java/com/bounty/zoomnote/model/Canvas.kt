package com.bounty.zoomnote.model

import java.util.UUID

data class Canvas(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)
