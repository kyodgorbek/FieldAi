package org.yodgorbek.fieldai.shared.domain.authentication

interface SecureStorage {
    fun saveToken(key: String, token: String)
    fun getToken(key: String): String?
    fun deleteToken(key: String)
}
