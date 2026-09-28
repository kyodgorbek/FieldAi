package org.yodgorbek.fieldai.shared.di

import org.koin.dsl.module
import org.yodgorbek.fieldai.shared.data.native.IosBluetoothManager
import org.yodgorbek.fieldai.shared.data.native.IosCalendarManager
import org.yodgorbek.fieldai.shared.data.native.IosContactsManager
import org.yodgorbek.fieldai.shared.data.native.IosDocumentScanner
import org.yodgorbek.fieldai.shared.data.native.IosLocationManager
import org.yodgorbek.fieldai.shared.data.native.IosPushNotificationManager
import org.yodgorbek.fieldai.shared.data.native.IosVoiceManager
import org.yodgorbek.fieldai.shared.domain.native.BluetoothManager
import org.yodgorbek.fieldai.shared.domain.native.CalendarManager
import org.yodgorbek.fieldai.shared.domain.native.ContactsManager
import org.yodgorbek.fieldai.shared.domain.native.DocumentScanner
import org.yodgorbek.fieldai.shared.domain.native.LocationManager
import org.yodgorbek.fieldai.shared.domain.native.PushNotificationManager
import org.yodgorbek.fieldai.shared.domain.native.VoiceManager

actual val platformModule = module {
    single<VoiceManager> { IosVoiceManager() }
    single<DocumentScanner> { IosDocumentScanner() }
    single<ContactsManager> { IosContactsManager() }
    single<LocationManager> { IosLocationManager() }
    single<CalendarManager> { IosCalendarManager() }
    single<BluetoothManager> { IosBluetoothManager() }
    single<PushNotificationManager> { IosPushNotificationManager() }
}
