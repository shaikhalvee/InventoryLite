package com.inventory.inventorylite.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Role { ADMIN, CLERK, VIEWER }

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHashB64: String,
    val saltB64: String,
    val role: Role,
    val isActive: Boolean = true,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

/**
 * Single-row session table (id is always 1).
 * If currentUserId is null => logged out.
 */
@Entity(tableName = "user_session")
data class SessionEntity(
    @PrimaryKey val id: Int = 1,
    val currentUserId: Long?
)

data class SessionUser(
    val userId: Long?,
    val username: String?,
    val role: Role?
)
