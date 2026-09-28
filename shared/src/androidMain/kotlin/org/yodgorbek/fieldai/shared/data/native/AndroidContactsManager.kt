package org.yodgorbek.fieldai.shared.data.native

import android.content.Context
import android.provider.ContactsContract
import org.yodgorbek.fieldai.shared.domain.native.Contact
import org.yodgorbek.fieldai.shared.domain.native.ContactsManager

class AndroidContactsManager(
    private val context: Context
) : ContactsManager {
    override suspend fun getContacts(): List<Contact> {
        if (!hasContactsPermission()) return emptyList()
        
        val contacts = mutableListOf<Contact>()
        val contentResolver = context.contentResolver
        
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        
        cursor?.use {
            if (it.count > 0) {
                while (it.moveToNext()) {
                    val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                    
                    if (idIndex == -1 || nameIndex == -1 || hasPhoneIndex == -1) continue
                    
                    val id = it.getString(idIndex)
                    val name = it.getString(nameIndex) ?: ""
                    val hasPhoneNumber = it.getInt(hasPhoneIndex) > 0
                    
                    val phoneNumbers = mutableListOf<String>()
                    if (hasPhoneNumber) {
                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(id),
                            null
                        )
                        phoneCursor?.use { pCursor ->
                            val numberIndex = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numberIndex != -1) {
                                while (pCursor.moveToNext()) {
                                    phoneNumbers.add(pCursor.getString(numberIndex))
                                }
                            }
                        }
                    }
                    
                    val emails = mutableListOf<String>()
                    val emailCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                        arrayOf(id),
                        null
                    )
                    emailCursor?.use { eCursor ->
                        val emailIndex = eCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                        if (emailIndex != -1) {
                            while (eCursor.moveToNext()) {
                                emails.add(eCursor.getString(emailIndex))
                            }
                        }
                    }
                    
                    contacts.add(Contact(id, name, emails, phoneNumbers))
                }
            }
        }
        return contacts
    }

    override fun hasContactsPermission(): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
