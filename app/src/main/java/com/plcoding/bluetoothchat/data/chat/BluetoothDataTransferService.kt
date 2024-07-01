package com.plcoding.bluetoothchat.data.chat

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.github.eltonvs.obd.command.control.VINCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection

class BluetoothDataTransferService(
    private val socket: BluetoothSocket
) {
    private val obdConnection = ObdDeviceConnection(socket.inputStream, socket.outputStream)

    suspend fun getVIN(): String{
        val tempVIN = obdConnection.run(VINCommand())
        Log.i("BluetoothDataTransferService.getVIN()", "VIN: $tempVIN")
        return tempVIN.value
    }
}