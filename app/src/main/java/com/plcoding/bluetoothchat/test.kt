package com.plcoding.bluetoothchat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import com.github.eltonvs.obd.command.AdaptiveTimingMode
import com.github.eltonvs.obd.command.BadResponseException
import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdProtocols
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.at.AdapterVoltageCommand
import com.github.eltonvs.obd.command.at.BufferDumpCommand
import com.github.eltonvs.obd.command.at.BypassInitializationCommand
import com.github.eltonvs.obd.command.at.DescribeProtocolCommand
import com.github.eltonvs.obd.command.at.DescribeProtocolNumberCommand
import com.github.eltonvs.obd.command.at.IgnitionMonitorCommand
import com.github.eltonvs.obd.command.at.LowPowerModeCommand
import com.github.eltonvs.obd.command.at.ProtocolCloseCommand
import com.github.eltonvs.obd.command.at.ResetAdapterCommand
import com.github.eltonvs.obd.command.at.SelectProtocolCommand
import com.github.eltonvs.obd.command.at.SetAdaptiveTimingCommand
import com.github.eltonvs.obd.command.at.SetEchoCommand
import com.github.eltonvs.obd.command.at.SetHeadersCommand
import com.github.eltonvs.obd.command.at.SetLineFeedCommand
import com.github.eltonvs.obd.command.at.SetSpacesCommand
import com.github.eltonvs.obd.command.at.SetTimeoutCommand
import com.github.eltonvs.obd.command.at.SlowInitiationCommand
import com.github.eltonvs.obd.command.at.WarmStartCommand
import com.github.eltonvs.obd.command.control.AvailablePIDsCommand
import com.github.eltonvs.obd.command.control.BaseMonitorStatus
import com.github.eltonvs.obd.command.control.BaseTroubleCodesCommand
import com.github.eltonvs.obd.command.control.DTCNumberCommand
import com.github.eltonvs.obd.command.control.DistanceMILOnCommand
import com.github.eltonvs.obd.command.control.DistanceSinceCodesClearedCommand
import com.github.eltonvs.obd.command.control.MILOnCommand
import com.github.eltonvs.obd.command.control.ModuleVoltageCommand
import com.github.eltonvs.obd.command.control.MonitorStatusCurrentDriveCycleCommand
import com.github.eltonvs.obd.command.control.MonitorStatusSinceCodesClearedCommand
import com.github.eltonvs.obd.command.control.PendingTroubleCodesCommand
import com.github.eltonvs.obd.command.control.PermanentTroubleCodesCommand
import com.github.eltonvs.obd.command.control.ResetTroubleCodesCommand
import com.github.eltonvs.obd.command.control.TimeSinceCodesClearedCommand
import com.github.eltonvs.obd.command.control.TimeSinceMILOnCommand
import com.github.eltonvs.obd.command.control.TimingAdvanceCommand
import com.github.eltonvs.obd.command.control.TroubleCodesCommand
import com.github.eltonvs.obd.command.control.VINCommand
import com.github.eltonvs.obd.command.egr.CommandedEgrCommand
import com.github.eltonvs.obd.command.egr.EgrErrorCommand
import com.github.eltonvs.obd.command.engine.AbsoluteLoadCommand
import com.github.eltonvs.obd.command.engine.LoadCommand
import com.github.eltonvs.obd.command.engine.MassAirFlowCommand
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.command.engine.RelativeThrottlePositionCommand
import com.github.eltonvs.obd.command.engine.RuntimeCommand
import com.github.eltonvs.obd.command.engine.SpeedCommand
import com.github.eltonvs.obd.command.engine.ThrottlePositionCommand
import com.github.eltonvs.obd.command.fuel.CommandedEquivalenceRatioCommand
import com.github.eltonvs.obd.command.fuel.EthanolLevelCommand
import com.github.eltonvs.obd.command.fuel.FuelAirEquivalenceRatioCommand
import com.github.eltonvs.obd.command.fuel.FuelConsumptionRateCommand
import com.github.eltonvs.obd.command.fuel.FuelLevelCommand
import com.github.eltonvs.obd.command.fuel.FuelTrimCommand
import com.github.eltonvs.obd.command.fuel.FuelTypeCommand
import com.github.eltonvs.obd.command.pressure.BarometricPressureCommand
import com.github.eltonvs.obd.command.pressure.FuelPressureCommand
import com.github.eltonvs.obd.command.pressure.FuelRailGaugePressureCommand
import com.github.eltonvs.obd.command.pressure.FuelRailPressureCommand
import com.github.eltonvs.obd.command.pressure.IntakeManifoldPressureCommand
import com.github.eltonvs.obd.command.temperature.AirIntakeTemperatureCommand
import com.github.eltonvs.obd.command.temperature.AmbientAirTemperatureCommand
import com.github.eltonvs.obd.command.temperature.EngineCoolantTemperatureCommand
import com.github.eltonvs.obd.command.temperature.OilTemperatureCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID


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

    //Get the UUID of the bluetooth device
//    val uuid:UUID = device.uuids[0].uuid

    // Create a BluetoothSocket
    var bluetoothSocket: BluetoothSocket? = null
    try {
        bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    bluetoothAdapter.cancelDiscovery()

    try {
        bluetoothSocket!!.connect()
        // Connection successful
        // You can now manage your connection (in a separate thread)
    } catch (connectException: IOException) {
        connectException.printStackTrace()
        try {
            bluetoothSocket!!.close()
        } catch (closeException: IOException) {
            closeException.printStackTrace()
        }
    }

    // Manage the connection in a separate thread
    val inputStream = bluetoothSocket!!.inputStream
    val outputStream = bluetoothSocket!!.outputStream
    
//    try{
//        outputStream.write(1234)
//    } catch (writeException: IOException) {
//        writeException.printStackTrace()
//    }

    manageConnectedSocket(inputStream, outputStream)
}

fun manageConnectedSocket(
    inputStream:InputStream,
    outputStream: OutputStream
) = runBlocking {
    val TAG = "manageConnectedSocket"

    val obdConnection = ObdDeviceConnection(inputStream, outputStream)
    var response: ObdResponse
    var responseCommand: String
    var responseRaw: ObdRawResponse

    // Code to manage the connection in a separate thread
    launch {
        try {
            Log.i(TAG, "Sending Custom Command")
            response = obdConnection.run(MonitorAllCommand())
            responseCommand = response.command.name
            responseRaw = response.rawResponse
            Log.i(TAG, "$responseCommand: $responseRaw")
        } catch (obdException: RuntimeException) {
            obdException.printStackTrace() // more detail here with log statements
        }
    }
        launch {
////        Log.i(TAG, "AT COMMANDS TEST")
////        try{
////            Log.i(TAG, "Sending RESET ADAPTER")
////            response = obdConnection.run(ResetAdapterCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending WARM START")
////            response = obdConnection.run(WarmStartCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending SLOW INIT")
////            response = obdConnection.run(SlowInitiationCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending LOW POWER")
////            response = obdConnection.run(LowPowerModeCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending BUFFER DUMP")
////            response = obdConnection.run(BufferDumpCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending BYPASS INIT")
////            response = obdConnection.run(BypassInitializationCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending PROTOCOL CLOSE")
////            response = obdConnection.run(ProtocolCloseCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending DESCRIBE PROTOCOL")
////            response = obdConnection.run(DescribeProtocolCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending DESCRIBE PROTOCOL NUMBER")
////            response = obdConnection.run(DescribeProtocolNumberCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending IGNITION MONITOR")
////            response = obdConnection.run(IgnitionMonitorCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending ADAPTER VOLTAGE")
////            response = obdConnection.run(AdapterVoltageCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending SELECT PROTOCOL")
////            response = obdConnection.run(SelectProtocolCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending SetAdaptiveTiming")
////            response = obdConnection.run(SetAdaptiveTimingCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending SetEcho")
////            response = obdConnection.run(SetEchoCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending SetHeaders")
////            response = obdConnection.run(SetHeadersCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending SetLineFeed")
////            response = obdConnection.run(SetLineFeedCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending SetSpaces")
////            response = obdConnection.run(SetSpacesCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending SetTimeout")
////            response = obdConnection.run(SetTimeoutCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        Log.i(TAG, "CONTROL COMMANDS TEST")
////        try{
////            Log.i(TAG, "Sending Available PIDS 1-20")
////            response = obdConnection.run(AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_01_TO_20))
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Available PIDS21-40")
////            response = obdConnection.run(AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_21_TO_40))
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Available PIDS 41-60")
////            response = obdConnection.run(AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_41_TO_60))
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Available PIDS 61-80")
////            response = obdConnection.run(AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_61_TO_80))
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Available PIDS 81-A0")
////            response = obdConnection.run(AvailablePIDsCommand(AvailablePIDsCommand.AvailablePIDsRanges.PIDS_81_TO_A0))
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending MODULE VOLTAGE")
////            response = obdConnection.run(ModuleVoltageCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending TimingAdvance")
////            response = obdConnection.run(TimingAdvanceCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending VINCommand")
////            response = obdConnection.run(VINCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending MIL ON")
////            response = obdConnection.run(MILOnCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending DistanceMIL ON")
////            response = obdConnection.run(DistanceMILOnCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending TimeSinceMIL ON")
////            response = obdConnection.run(TimeSinceMILOnCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending MonitorStatusSinceCodesCleared")
////            response = obdConnection.run(MonitorStatusSinceCodesClearedCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending MonitorStatusCurrentDriveCycle")
////            response = obdConnection.run(MonitorStatusCurrentDriveCycleCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending DTC Number")
////            response = obdConnection.run(DTCNumberCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending DistanceSinceCodesCleared")
////            response = obdConnection.run(DistanceSinceCodesClearedCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending TimeSinceCodesCleared")
////            response = obdConnection.run(TimeSinceCodesClearedCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending ResetTroubleCodes")
////            response = obdConnection.run(ResetTroubleCodesCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Trouble Codes")
////            response = obdConnection.run(TroubleCodesCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Pending Trouble Codes")
////            response = obdConnection.run(PendingTroubleCodesCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Permanent Trouble Codes")
////            response = obdConnection.run(PermanentTroubleCodesCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        Log.i(TAG, "EGR COMMANDS TEST")
////        try{
////            Log.i(TAG, "Sending Commanded EGR")
////            response = obdConnection.run(CommandedEgrCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending EGR ERROR")
////            response = obdConnection.run(EgrErrorCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        Log.i(TAG,"ENGINE COMMAND TEST")
////        try{
////            Log.i(TAG, "Sending Speed")
////            response = obdConnection.run(SpeedCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending RPM")
////            response = obdConnection.run(RPMCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending MassAirFlow")
////            response = obdConnection.run(MassAirFlowCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Runtime")
////            response = obdConnection.run(RuntimeCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Load")
////            response = obdConnection.run(LoadCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Absolute Load")
////            response = obdConnection.run(AbsoluteLoadCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Throttle Position")
////            response = obdConnection.run(ThrottlePositionCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Relative Throttle Position")
////            response = obdConnection.run(RelativeThrottlePositionCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        Log.i(TAG, "FUEL COMMAND TEST")
////        try{
////            Log.i(TAG, "Sending Fuel Consumption")
////            response = obdConnection.run(FuelConsumptionRateCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Fuel Type")
////            response = obdConnection.run(FuelTypeCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Fuel Level")
////            response = obdConnection.run(FuelLevelCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Ethanol Level")
////            response = obdConnection.run(EthanolLevelCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
//////        try{
//////            Log.i(TAG, "Sending FuelTrim")
//////            response = obdConnection.run(FuelTrimCommand())
//////            responseCommand = response.command.name
//////            responseRaw = response.rawResponse
//////            Log.i(TAG, "$responseCommand: $responseRaw")
//////        }catch (obdException: RuntimeException){
//////            obdException.printStackTrace() // more detail here with log statements
//////        }
////        try{
////            Log.i(TAG, "Sending Commanded Equivalence Ratio")
////            response = obdConnection.run(CommandedEquivalenceRatioCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
//////        try{
//////            Log.i(TAG, "Sending FuelAirEquivalenceRatio")
//////            response = obdConnection.run(FuelAirEquivalenceRatioCommand())
//////            responseCommand = response.command.name
//////            responseRaw = response.rawResponse
//////            Log.i(TAG, "$responseCommand: $responseRaw")
//////        }catch (obdException: RuntimeException){
//////            obdException.printStackTrace() // more detail here with log statements
//////        }
////        Log.i(TAG, "PRESSURE COMMANDS TEST")
////        try{
////            Log.i(TAG, "Sending Barometric")
////            response = obdConnection.run(BarometricPressureCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending IntakeManifold")
////            response = obdConnection.run(IntakeManifoldPressureCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending FuelPressure")
////            response = obdConnection.run(FuelPressureCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending FuelRail")
////            response = obdConnection.run(FuelRailPressureCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending FuelRailGauge")
////            response = obdConnection.run(FuelRailGaugePressureCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        Log.i(TAG, "TEMPERATURE COMMANDS TEST")
////        try{
////            Log.i(TAG, "Sending AirIntake Temp")
////            response = obdConnection.run(AirIntakeTemperatureCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending AmbientAir Temp")
////            response = obdConnection.run(AmbientAirTemperatureCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending EngineCoolant Temp")
////            response = obdConnection.run(EngineCoolantTemperatureCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
////        try{
////            Log.i(TAG, "Sending Oil Temp")
////            response = obdConnection.run(OilTemperatureCommand())
////            responseCommand = response.command.name
////            responseRaw = response.rawResponse
////            Log.i(TAG, "$responseCommand: $responseRaw")
////        }catch (obdException: RuntimeException){
////            obdException.printStackTrace() // more detail here with log statements
////        }
//    }
        }
    return@runBlocking
}

class CustomCommand : ObdCommand() {
    // Required
    override val tag = "CUSTOM_COMMAND"
    override val name = "Custom Command"
    override val mode = "AT"
    override val pid = "Hello World!"

    //Optional
    override val defaultUnit = ""
    override val handler = { it: ObdRawResponse -> "Calculations to parse value from ${it.processedValue}" }
}

class ResetAllCommand : ObdCommand() {
    // Required
    override val tag = "RESET_ALL_COMMAND"
    override val name = "Reset All Command"
    override val mode = "AT"
    override val pid = "Z"

    //Optional
    override val defaultUnit = ""
    override val handler = { it: ObdRawResponse -> "Calculations to parse value from ${it.processedValue}" }
}

class ResetToDefaultCommand : ObdCommand() {
    // Required
    override val tag = "RESET_TO_DEFAULT_COMMAND"
    override val name = "Reset To Default Command"
    override val mode = "AT"
    override val pid = "D"

    //Optional
    override val defaultUnit = ""
    override val handler = { it: ObdRawResponse -> "Calculations to parse value from ${it.processedValue}" }
}

class LineFeedsOnCommand : ObdCommand() {
    // Required
    override val tag = "LINE_FEEDS_ON_COMMAND"
    override val name = "Line Feeds On Command"
    override val mode = "AT"
    override val pid = "L1"

    //Optional
    override val defaultUnit = ""
    override val handler = { it: ObdRawResponse -> "Calculations to parse value from ${it.processedValue}" }
}

class CANSilentMonitoringOnCommand : ObdCommand() {
    // Required
    override val tag = "CAN_SILENT_MONITORING_ON_COMMAND"
    override val name = "CAN Silent Monitoring On Command"
    override val mode = "AT"
    override val pid = "CSM1"

    //Optional
    override val defaultUnit = ""
    override val handler = { it: ObdRawResponse -> "Calculations to parse value from ${it.processedValue}" }
}

class MonitorAllCommand : ObdCommand() {
    // Required
    override val tag = "MONITOR_ALL_COMMAND"
    override val name = "Monitor All Command"
    override val mode = "AT"
    override val pid = "MA"

    //Optional
    override val defaultUnit = ""
    override val handler = { it: ObdRawResponse -> "Calculations to parse value from ${it.processedValue}" }
}