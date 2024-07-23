package com.plcoding.bluetoothchat.kotlinapi.command

import com.plcoding.bluetoothchat.kotlinapi.command.RegexPatterns.BUSINIT_ERROR_MESSAGE_PATTERN
import com.plcoding.bluetoothchat.kotlinapi.command.RegexPatterns.DIGITS_LETTERS_PATTERN
import com.plcoding.bluetoothchat.kotlinapi.command.RegexPatterns.ERROR_MESSAGE_PATTERN
import com.plcoding.bluetoothchat.kotlinapi.command.RegexPatterns.MISUNDERSTOOD_COMMAND_MESSAGE_PATTERN
import com.plcoding.bluetoothchat.kotlinapi.command.RegexPatterns.NO_DATE_MESSAGE_PATTERN
import com.plcoding.bluetoothchat.kotlinapi.command.RegexPatterns.STOPPED_MESSAGE_PATERN
import com.plcoding.bluetoothchat.kotlinapi.command.RegexPatterns.UNABLE_TO_CONNECT_MESSAGE_PATTERN
import com.plcoding.bluetoothchat.kotlinapi.command.RegexPatterns.UNSUPPORTED_COMMAND_MESSAGE_PATTERN
import com.plcoding.bluetoothchat.kotlinapi.command.RegexPatterns.WHITESPACE_PATTERN


private fun String.sanitize(): String = removeAll(
    WHITESPACE_PATTERN,
    this
).uppercase()

abstract class BadResponseException(private val command: ObdCommand, private val response: ObdRawResponse) :
    RuntimeException() {
    companion object {
        fun checkForExceptions(command: ObdCommand, response: ObdRawResponse): ObdRawResponse =
            with(response.value.sanitize()) {
                when {
                    contains(BUSINIT_ERROR_MESSAGE_PATTERN.sanitize()) ->
                        throw BusInitException(command, response)
                    contains(MISUNDERSTOOD_COMMAND_MESSAGE_PATTERN.sanitize()) ->
                        throw MisunderstoodCommandException(command, response)
                    contains(NO_DATE_MESSAGE_PATTERN.sanitize()) ->
                        throw NoDataException(command, response)
                    contains(STOPPED_MESSAGE_PATERN.sanitize()) ->
                        throw StoppedException(command, response)
                    contains(UNABLE_TO_CONNECT_MESSAGE_PATTERN.sanitize()) ->
                        throw UnableToConnectException(command, response)
                    contains(ERROR_MESSAGE_PATTERN.sanitize()) ->
                        throw UnknownErrorException(command, response)
                    matches(UNSUPPORTED_COMMAND_MESSAGE_PATTERN.toRegex()) ->
                        throw UnSupportedCommandException(command, response)
                    !command.skipDigitCheck && !matches(DIGITS_LETTERS_PATTERN.toRegex()) ->
                        throw NonNumericResponseException(command, response)
                    else -> response
                }
            }
    }

    override fun toString(): String =
        "${this.javaClass.simpleName} while executing command [${command.tag}], response [${response.value}]"
}


private typealias BRE = BadResponseException

class NonNumericResponseException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
class BusInitException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
class MisunderstoodCommandException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
class NoDataException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
class StoppedException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
class UnableToConnectException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
class UnknownErrorException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
class UnSupportedCommandException(command: ObdCommand, response: ObdRawResponse) : BRE(command, response)
