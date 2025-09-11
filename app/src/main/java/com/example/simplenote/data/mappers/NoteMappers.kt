package com.example.simplenote.data.mappers

import com.example.simplenote.data.local.NoteEntity
import com.example.simplenote.data.remote.NoteDto

fun NoteDto.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    description = description,
    color = color,
    created_at = created_at,
    updated_at = updated_at,
    pendingCreate = false,
    pendingUpdate = false
)

fun NoteEntity.toDto(): NoteDto = NoteDto(
    id = id,
    title = title,
    description = description,
    color = color,
    created_at = created_at,
    updated_at = updated_at
)
