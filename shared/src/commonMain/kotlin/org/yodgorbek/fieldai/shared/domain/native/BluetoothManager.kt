package org.yodgorbek.fieldai.shared.domain.native

import kotlinx.coroutines.flow.Flow

data class BluetoothDevice(
    val id: String,
    val name: String?,
    val isConnected: Boolean
)

interface BluetoothManager {
    /**
     * Scan for nearby devices.
     */
    fun scanDevices(): Flow<List<BluetoothDevice>>
    
    /**
     * Connect to a specific device.
     */
    suspend fun connectToDevice(deviceId: String): Boolean
    
    /**
     * Disconnect from a device.
     */
    suspend fun disconnect(deviceId: String)
}
