package org.yodgorbek.fieldai.shared.domain.native

data class Contact(
    val id: String,
    val name: String,
    val emails: List<String>,
    val phoneNumbers: List<String>
)

interface ContactsManager {
    /**
     * Get all contacts. May require permission.
     */
    suspend fun getContacts(): List<Contact>
    
    fun hasContactsPermission(): Boolean
}
