package com.inventory.inventorylite.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthDao {
    // Users
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun userCount(): Int

    @Query("SELECT id, username, role, isActive FROM users ORDER BY username COLLATE NOCASE")
    fun observeUsers(): Flow<List<User>>

    // Session
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: SessionEntity)

    @Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
    fun observeSession(): Flow<SessionEntity?>

    @Query("""
        SELECT s.currentUserId AS userId, u.username AS username, u.role AS role
        FROM user_session s
        LEFT JOIN users u ON u.id = s.currentUserId
        WHERE s.id = 1
        LIMIT 1
    """)
    fun observeSessionUser(): Flow<SessionUser?>
}
