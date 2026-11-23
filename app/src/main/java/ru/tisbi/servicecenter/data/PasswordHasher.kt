package ru.tisbi.servicecenter.data

// Хеширование паролей перед записью в базу.

import java.security.MessageDigest

/**
 * Пароли в базе не хранятся в открытом виде: сохраняется SHA-256 от пароля
 * и служебной соли приложения.
 */
object PasswordHasher {
    private const val SALT = "holodservice-v1"

    fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest((password + SALT).toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
