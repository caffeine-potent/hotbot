package me.aberrantfox.aegeus.commandframework.commands

import me.aberrantfox.aegeus.commandframework.ArgumentType
import me.aberrantfox.aegeus.commandframework.Command
import me.aberrantfox.aegeus.commandframework.CommandEvent


enum class SecurityLevel {
    Normal, Elevated, High, Max;
}

fun names() = SecurityLevel.values().map { it.name }

object SecurityLevelState {
    var alertLevel: SecurityLevel = SecurityLevel.Normal
}

@Command(ArgumentType.String)
fun setSecurityLevel(event: CommandEvent) {
    val targetLevel = (event.args[0] as String).toUpperCase()

    try {
        val parsed = SecurityLevel.valueOf(targetLevel)
        SecurityLevelState.alertLevel = parsed
        event.respond("Level set to ${parsed.name}")
    } catch (e: IllegalArgumentException) {
        event.respond("SecurityLevel: $targetLevel is unknown, known levels are: ${names()}")
    }
}

@Command
fun securityLevel(event: CommandEvent) = event.respond("Current security level: ${SecurityLevelState.alertLevel}")