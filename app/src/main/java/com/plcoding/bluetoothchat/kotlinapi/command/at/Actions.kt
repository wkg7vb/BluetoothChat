package com.plcoding.bluetoothchat.kotlinapi.command.at

import com.plcoding.bluetoothchat.kotlinapi.command.ATCommand


class ResetAdapterCommand : ATCommand() {
    override val tag = "RESET_ADAPTER"
    override val name = "Reset OBD Adapter"
    override val pid = "Z"
}

class WarmStartCommand : ATCommand() {
    override val tag = "WARM_START"
    override val name = "OBD Warm Start"
    override val pid = "WS"
}

class SlowInitiationCommand : ATCommand() {
    override val tag = "SLOW_INITIATION"
    override val name = "OBD Slow Initiation"
    override val pid = "SI"
}

class LowPowerModeCommand : ATCommand() {
    override val tag = "LOW_POWER_MODE"
    override val name = "OBD Low Power Mode"
    override val pid = "LP"
}

class BufferDumpCommand : ATCommand() {
    override val tag = "BUFFER_DUMP"
    override val name = "OBD Buffer Dump"
    override val pid = "BD"
}

class BypassInitializationCommand : ATCommand() {
    override val tag = "BYPASS_INITIALIZATION"
    override val name = "OBD Bypass Initialization Sequence"
    override val pid = "BI"
}

class ProtocolCloseCommand : ATCommand() {
    override val tag = "PROTOCOL_CLOSE"
    override val name = "OBD Protocol Close"
    override val pid = "PC"
}

//added for my sake

class ATE0Command : ATCommand() {
    override val tag = "ATE0"
    override val name = "ATE0"
    override val pid = "E0"
}

class ATL0Command : ATCommand() {
    override val tag = "ATL0"
    override val name = "ATL0"
    override val pid = "L0"
}

class ATS1Command : ATCommand() {
    override val tag = "ATS1"
    override val name = "ATS1"
    override val pid = "S1"
}

class ATAT0Command : ATCommand() {
    override val tag = "AT0"
    override val name = "AT0"
    override val pid = "AT0"
}

class ATSP0Command : ATCommand() {
    override val tag = "ATSP0"
    override val name = "ATSP0"
    override val pid = "SP0"
}

class ATLPCommand : ATCommand() {
    override val tag = "ATLP"
    override val name = "ATLP"
    override val pid = "LP"
}
