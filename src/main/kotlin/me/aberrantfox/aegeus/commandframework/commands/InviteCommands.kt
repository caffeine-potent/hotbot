package me.aberrantfox.aegeus.commandframework.commands

import me.aberrantfox.aegeus.commandframework.ArgumentType
import me.aberrantfox.aegeus.commandframework.CommandSet
import me.aberrantfox.aegeus.dsls.command.commands
import me.aberrantfox.aegeus.listeners.antispam.RecentInvites

@CommandSet
fun inviteCommands() =
    commands {
        command("whitelistinvite") {
            expect(ArgumentType.Word)
            execute {
                val inv = it.args[0] as String
                RecentInvites.ignore.add(inv)
                it.respond("Added $inv to the invite whitelist, it can now be posted freely.")
            }
        }

        command("unwhitelistinvite") {
            expect(ArgumentType.Word)
            execute {
                val inv = it.args[0] as String
                RecentInvites.ignore.remove(inv)
                it.respond("$inv has been removed from the invite whitelist, posting it the configured amount of times will result in a ban.")
            }
        }
    }
