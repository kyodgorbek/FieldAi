package org.yodgorbek.fieldai.shared.di

import org.koin.dsl.module
import org.yodgorbek.fieldai.shared.data.native.AndroidBluetoothManager
import org.yodgorbek.fieldai.shared.data.native.AndroidCalendarManager
import org.yodgorbek.fieldai.shared.data.native.AndroidContactsManager
import org.yodgorbek.fieldai.shared.data.native.AndroidDocumentScanner
import org.yodgorbek.fieldai.shared.data.native.AndroidLocationManager
import org.yodgorbek.fieldai.shared.data.native.AndroidPushNotificationManager
import org.yodgorbek.fieldai.shared.data.native.AndroidVoiceManager
import org.yodgorbek.fieldai.shared.domain.native.BluetoothManager
import org.yodgorbek.fieldai.shared.domain.native.CalendarManager
import org.yodgorbek.fieldai.shared.domain.native.ContactsManager
import org.yodgorbek.fieldai.shared.domain.native.DocumentScanner
import org.yodgorbek.fieldai.shared.domain.native.LocationManager
import org.yodgorbek.fieldai.shared.domain.native.PushNotificationManager
import org.yodgorbek.fieldai.shared.domain.native.VoiceManager

actual val platformModule = module {
    single<VoiceManager> { AndroidVoiceManager(get()) }
    single<DocumentScanner> { AndroidDocumentScanner(get()) }
    single<ContactsManager> { AndroidContactsManager(get()) }
    single<LocationManager> { AndroidLocationManager(get()) }
    single<CalendarManager> { AndroidCalendarManager(get()) }
    single<BluetoothManager> { AndroidBluetoothManager(get()) }
    single<PushNotificationManager> { AndroidPushNotificationManager(get()) }
}
