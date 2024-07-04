package com.plcoding.bluetoothchat.domain.chat

import java.util.UUID

typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val name: String?,
    val address: String
)
