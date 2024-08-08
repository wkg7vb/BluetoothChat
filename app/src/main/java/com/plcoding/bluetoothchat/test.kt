package com.plcoding.bluetoothchat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Chronometer
import com.plcoding.bluetoothchat.kotlinapi.command.ObdCommand
import com.plcoding.bluetoothchat.kotlinapi.command.ObdRawResponse
import com.plcoding.bluetoothchat.kotlinapi.command.ObdResponse
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATAT0Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATE0Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATL0Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATLPCommand
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATS1Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ATSP0Command
import com.plcoding.bluetoothchat.kotlinapi.command.at.ResetAdapterCommand
import com.plcoding.bluetoothchat.kotlinapi.command.control.AvailablePIDsCommand
import com.plcoding.bluetoothchat.kotlinapi.command.control.VINCommand
import com.plcoding.bluetoothchat.kotlinapi.command.engine.RPMCommand
import com.plcoding.bluetoothchat.kotlinapi.command.engine.RelativeThrottlePositionCommand
import com.plcoding.bluetoothchat.kotlinapi.command.engine.SpeedCommand
import com.plcoding.bluetoothchat.kotlinapi.command.engine.ThrottlePositionCommand
import com.plcoding.bluetoothchat.kotlinapi.connection.ObdDeviceConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.UnknownHostException
import java.util.UUID
import kotlin.system.measureTimeMillis


// UUID for SPP (Serial Port Profile)
private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

@SuppressLint("MissingPermission")
fun createBluetoothConnection(
    deviceAddress: String,
    bluetoothAdapter: BluetoothAdapter
){
    val TAG = "createBluetoothConnection"
    Log.i(TAG, "createBluetoothConnection()")


    if (bluetoothAdapter == null) {
        // Device doesn't support Bluetooth
        Log.i(TAG,"Device doesn't support Bluetooth")
    }

    if (!bluetoothAdapter.isEnabled) {
        // Bluetooth is not enabled
        // You might want to ask the user to enable it
        Log.i(TAG,"Bluetooth is not enabled")
    }

    // Get the BluetoothDevice object
    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)

    // Create a BluetoothSocket
    var bluetoothSocket: BluetoothSocket? = null
    try {
        bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    bluetoothAdapter.cancelDiscovery()

    try {
        bluetoothSocket?.connect()
        // Connection successful
        // You can now manage your connection (in a separate thread)
    } catch (connectException: IOException) {
        connectException.printStackTrace()
        try {
            bluetoothSocket?.close()
        } catch (closeException: IOException) {
            closeException.printStackTrace()
        }
    }

    // Manage the connection in a separate thread
    val inputStream = bluetoothSocket!!.inputStream
    val outputStream = bluetoothSocket!!.outputStream

    Log.i(TAG, "Opening API with Bluetooth Device")
    manageConnectedSocket(inputStream, outputStream)

    inputStream.close()
    outputStream.close()
    bluetoothSocket.close()
}

fun createWifiConnection(
    hostIP: String,
    portAdd: Int
) {
    val TAG = "createWifiConnection"
    try {
        val socket = Socket(hostIP, portAdd)
        val outputStream: OutputStream = socket.getOutputStream()
        val inputStream: InputStream = socket.getInputStream()

        Log.i(TAG, "Opening API with Network Device")
        manageConnectedSocket(inputStream, outputStream)

        inputStream.close()
        outputStream.close()
        socket.close()
    }  catch (e: UnknownHostException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return
}

fun manageConnectedSocket(
    inputStream:InputStream,
    outputStream: OutputStream
) = runBlocking {
    val TAG = "manageConnectedSocket"

    val obdConnection = ObdDeviceConnection(inputStream, outputStream)

    // Code to manage the connection in a separate thread
    launch {
        logCommand(ResetAdapterCommand(), obdConnection)
        delay(2500)
        logCommand(ATE0Command(), obdConnection)
        logCommand(ATL0Command(), obdConnection)
        logCommand(ATS1Command(), obdConnection)
        logCommand(ATAT0Command(), obdConnection)
        logCommand(ATSP0Command(), obdConnection)

        var tests = 1000
        var successes = 0
        var duration = measureTimeMillis {
            repeat(tests) {
                if (logCommand(SpeedCommand(), obdConnection)){
                    successes++
                }
            }
        }
        logCommand(ATLPCommand(), obdConnection)
        var succPS = (successes.toDouble() / (duration.toDouble() / 1000.toDouble()))
        var succPer = (1 - (successes.toDouble()/tests.toDouble()))*100
        Log.i(TAG, "Successful Commands Per Second: $succPS")
        Log.i(TAG, "Percent Commands Failed: $succPer")

        return@launch
    }
    return@runBlocking
}

suspend fun logCommand(inCommand: ObdCommand, obdConnection: ObdDeviceConnection): Boolean{
    var response: ObdResponse
    var responseCommand: String
    var responseRaw: ObdRawResponse
    var responseValue: String
    var comName: String = inCommand.name

    try {
        response = obdConnection.run(inCommand)
        responseCommand = response.command.name
        responseRaw = response.rawResponse
        responseValue = response.formattedValue
        Log.i(comName , "$responseCommand          -:-         $responseRaw        -:-         $responseValue")
    } catch (obdException: RuntimeException) {
        //obdException.printStackTrace()
        Log.i(comName, "$comName FAILED")
        return false
    }
    return true
}