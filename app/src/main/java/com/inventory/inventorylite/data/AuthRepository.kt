package com.inventory.inventorylite.data

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val dao: AuthDao) {

    fun observeSessionUser(): Flow<SessionUser?> = dao.observeSessionUser()
    fun observeUsers(): Flow<List<UserEntity>> = dao.observeUsers()

    suspend fun ensureDefaultAdmin() {
        if (dao.userCount() > 0) return

        val now = System.currentTimeMillis()
        val salt = Security.newSalt()
        val hash = Security.hashPassword("admin123".toCharArray(), salt)

        dao.insertUser(
            UserEntity(
                username = "admin",
                passwordHashB64 = Security.b64(hash),
                saltB64 = Security.b64(salt),
                role = Role.ADMIN,
                isActive = true,
                createdAtEpochMs = now,
                updatedAtEpochMs = now
            )
        )
        dao.upsertSession(SessionEntity(id = 1, currentUserId = null))
    }

    suspend fun signIn(usernameRaw: String, password: CharArray) {
        val username = usernameRaw.trim()
        val u = dao.getUserByUsername(username) ?: throw IllegalArgumentException("Invalid credentials")
        if (!u.isActive) throw IllegalStateException("User is inactive")

        val salt = Security.fromB64(u.saltB64)
        val expected = Security.fromB64(u.passwordHashB64)
        val actual = Security.hashPassword(password, salt)

        if (!expected.contentEquals(actual)) throw IllegalArgumentException("Invalid credentials")

        dao.upsertSession(SessionEntity(id = 1, currentUserId = u.id))
    }

    suspend fun signOut() {
        dao.upsertSession(SessionEntity(id = 1, currentUserId = null))
    }

    suspend fun createUser(adminRole: Role, usernameRaw: String, password: CharArray, role: Role) {
        if (adminRole != Role.ADMIN) throw IllegalStateException("Not authorized")

        val username = usernameRaw.trim()
        require(username.isNotBlank()) { "Username required" }
        require(password.isNotEmpty()) { "Password required" }

        val now = System.currentTimeMillis()
        val salt = Security.newSalt()
        val hash = Security.hashPassword(password, salt)

        try {
            dao.insertUser(
                UserEntity(
                    username = username,
                    passwordHashB64 = Security.b64(hash),
                    saltB64 = Security.b64(salt),
                    role = role,
                    isActive = true,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now
                )
            )
        } catch (_: SQLiteConstraintException) {
            throw IllegalArgumentException("Username already exists")
        }
    }
}
