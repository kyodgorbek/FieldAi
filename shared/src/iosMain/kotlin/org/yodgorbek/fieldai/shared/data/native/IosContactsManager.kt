package org.yodgorbek.fieldai.shared.data.native

import org.yodgorbek.fieldai.shared.domain.native.Contact
import org.yodgorbek.fieldai.shared.domain.native.ContactsManager
import platform.Contacts.CNContactEmailAddressesKey
import platform.Contacts.CNContactFamilyNameKey
import platform.Contacts.CNContactGivenNameKey
import platform.Contacts.CNContactPhoneNumbersKey
import platform.Contacts.CNContactStore
import platform.Contacts.CNEntityType
import platform.Contacts.CNPhoneNumber
import platform.Contacts.CNAuthorizationStatusAuthorized

import platform.Contacts.CNLabeledValue
import kotlinx.cinterop.ExperimentalForeignApi

class IosContactsManager : ContactsManager {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getContacts(): List<Contact> {
        val store = CNContactStore()
        val keysToFetch = listOf(
            CNContactGivenNameKey,
            CNContactFamilyNameKey,
            CNContactEmailAddressesKey,
            CNContactPhoneNumbersKey
        )
        val request = platform.Contacts.CNContactFetchRequest(keysToFetch = keysToFetch)
        
        val contacts = mutableListOf<Contact>()
        
        store.enumerateContactsWithFetchRequest(request, error = null) { contact, _ ->
            if (contact != null) {
                val id = contact.identifier
                val name = "${contact.givenName} ${contact.familyName}".trim()
                val emails = contact.emailAddresses.mapNotNull { (it as? CNLabeledValue)?.value?.toString() }
                val phoneNumbers = contact.phoneNumbers.mapNotNull { ((it as? CNLabeledValue)?.value as? CNPhoneNumber)?.stringValue }
                
                contacts.add(Contact(id, name, emails, phoneNumbers))
            }
        }
        
        return contacts
    }

    override fun hasContactsPermission(): Boolean {
        return CNContactStore.authorizationStatusForEntityType(CNEntityType.CNEntityTypeContacts) == CNAuthorizationStatusAuthorized
    }
}
