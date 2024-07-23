package com.plcoding.bluetoothchat.kotlinapi.command

import com.plcoding.bluetoothchat.kotlinapi.command.ObdCommand


abstract class ATCommand : ObdCommand() {
    override val mode = "AT"
    override val skipDigitCheck = true
}