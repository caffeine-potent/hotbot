package me.aberrantfox.hotbot.commandframework.commands

import me.aberrantfox.hotbot.commandframework.ArgumentType
import me.aberrantfox.hotbot.commandframework.CommandSet
import me.aberrantfox.hotbot.dsls.command.commands
import me.aberrantfox.hotbot.services.MessageService
import me.aberrantfox.hotbot.services.MessageType
import me.aberrantfox.hotbot.database.getReason
import me.aberrantfox.hotbot.database.updateOrSetReason
import me.aberrantfox.hotbot.dsls.embed.embed
import me.aberrantfox.hotbot.extensions.*
import me.aberrantfox.hotbot.services.Configuration
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.awt.Color
import java.io.File
import java.text.SimpleDateFormat

class ModerationCommands

@CommandSet
fun moderationCommands() = commands {
    command("nuke") {
        expect(ArgumentType.Integer)
        execute {
            val amount = it.args[0] as Int
            if (amount <= 0) {
                it.respond("Yea, what exactly is the point in nuking nothing... ?")
            } else {
                it.channel.history.retrievePast(amount + 1).queue { past ->
                    past.forEach { msg -> msg.delete().queue() }
                    it.respond("Be nice. No spam.")
                }
            }
        }
    }

    command("ignore") {
        expect(ArgumentType.Integer)
        execute {
            val config = it.config
            val target = it.args[0] as String

            if (config.security.ignoredIDs.contains(target)) {
                config.security.ignoredIDs.remove(target)
                it.respond("Unignored $target")
            } else {
                config.security.ignoredIDs.add(target)
                it.respond("$target? Who? What? Don't know what that is. ;)")
            }
        }
    }

    command("mute") {
        expect(ArgumentType.UserID, ArgumentType.Integer, ArgumentType.Sentence)
        execute {
            val args = it.args

            val user = (args[0] as String).idToUser(it.jda)
            val time = (args[1] as Int).toLong() * 1000 * 60
            val reason = args[2] as String

            muteMember(it.guild, user, time, reason, it.config, it.author)
        }
    }

    command("lockdown") {
        execute {
            val config = it.config
            config.security.lockDownMode = !config.security.lockDownMode
            it.respond("Lockdown mode is now set to: ${config.security.lockDownMode}.")
        }
    }

    command("prefix") {
        expect(ArgumentType.Word)
        execute {
            val newPrefix = it.args[0] as String
            it.config.serverInformation.prefix = newPrefix
            it.respond("Prefix is now $newPrefix. Please invoke commands using that prefix in the future." +
                "To save this configuration, use the saveconfigurations command.")
            it.jda.presence.setPresence(OnlineStatus.ONLINE, Game.of("${it.config.serverInformation.prefix}help"))
        }
    }

    command("setfilter") {
        expect(ArgumentType.Word)
        execute {
            val desiredLevel = (it.args[0] as String).toRole(it.guild)

            if (desiredLevel == null) {
                it.respond("Don't know that permission level boss... ")
            } else {
                it.config.permissionedActions.commandMention = desiredLevel.id
                it.respond("Permission level now set to: ${desiredLevel.name} ; be sure to save configurations.")
            }
        }

    }
    command("move") {
        expect(ArgumentType.Word, ArgumentType.Integer, ArgumentType.Word)
        execute {
            val args = it.args

            val targets = getTargets((args[0] as String))
            val searchSpace = args[1] as Int
            val chan = args[2] as String

            if (searchSpace < 0) {
                it.respond("... move what")
                return@execute
            }

            if (searchSpace > 99) {
                it.respond("Yea buddy, I'm not moving the entire channel into another, 99 messages or less")
                return@execute
            }

            val channel = it.guild.textChannels.firstOrNull { it.id == chan }

            if (channel == null) {
                it.respond("... to where?")
                return@execute
            }

            it.message.delete().queue()

            it.channel.history.retrievePast(searchSpace + 1).queue { past ->
                handleResponse(past, channel, targets, it.channel, it.author.asMention, it.config)
            }
        }
    }
    command("badname") {
        expect(ArgumentType.UserID, ArgumentType.Sentence)
        execute {
            val args = it.args
            val target = args[0] as String
            val reason = args[1] as String

            val targetMember = it.guild.getMemberById(target)

            it.guild.controller.setNickname(targetMember, MessageService.getMessage(MessageType.Name)).queue {
                targetMember.user.openPrivateChannel().queue {
                    it.sendMessage("Your name has been changed forcefully by a member of staff for reason: $reason").queue()
                }
            }
        }
    }
    command("joindate") {
        expect(ArgumentType.UserID)
        execute {
            val target = it.args[0] as String

            val member = it.guild.getMemberById(target)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val joinDateParsed = dateFormat.parse(member.joinDate.toString())
            val joinDate = dateFormat.format(joinDateParsed)

            it.respond("${member.fullName()}'s join date: $joinDate")
        }
    }
    command("restart") {
        execute {
            val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
            val currentJar = File(ModerationCommands::class.java.protectionDomain.codeSource.location.toURI())

            if (!currentJar.name.endsWith(".jar")) return@execute

            ProcessBuilder(arrayListOf(javaBin, "-jar", currentJar.path)).start()
            System.exit(0)
        }
    }
    command("setbanreason") {
        expect(ArgumentType.Word, ArgumentType.Sentence)
        execute {
            val target = (it.args[0] as String)
            val reason = it.args[1] as String

            try {
                it.jda.performActionIfIsID(target) { user ->
                    updateOrSetReason(target, reason, it.author.id)
                    it.respond("The ban reason for $target has been logged")
                }
            } catch (e: IllegalArgumentException) {
                it.respond("$target is not a valid ID")
            }
        }
    }
    command("getbanreason") {
        expect(ArgumentType.Word)
        execute {
            val target = it.args[0] as String

            try {
                it.jda.performActionIfIsID(target) { user ->
                    val record = getReason(target)

                    if (record != null) {
                        it.respond("$target was banned by ${record.mod.idToUser(it.jda).fullName()} for reason ${record.reason}")
                    } else {
                        it.respond("That user does not have a record logged.")
                    }

                }
            } catch (e: IllegalArgumentException) {
                it.respond("$target is not a valid ID")
            }
        }
    }
}

private fun handleResponse(past: List<Message>, channel: MessageChannel, targets: List<String>, error: MessageChannel,
                           source: String, config: Configuration) {

    val messages = if (past.firstOrNull()?.isCommandInvocation(config) == true)
                       // Without ++move command invocation message
                       past.subList(1, past.size).filter { targets.contains(it.author.id)}
                   else
                       /*
                       Without extra message that could've been the ++move message but wasn't.
                       Side effect of having to search searchSpace+1 because of queue/API request timings
                       causing the possibility but not guarantee of the ++move command invocation being
                       included in the past List.
                       */
                       past.subList(0, past.size - 1).filter {targets.contains(it.author.id)}

    if (messages.isEmpty()) {
        error.sendMessage("No messages found").queue()
        return
    }

    val responseEmbed = buildResponseEmbed(error, source, messages)

    channel.sendMessage(responseEmbed).queue {
        messages.forEach {
            it.delete().queue()
        }
    }
}

private fun buildResponseEmbed(orig: MessageChannel, sourceMod: String, messages: List<Message>) =
        embed {
            title("__Moved Messages__")

            ifield {
                name = "Source Channel"
                value = "<#${orig.id}>"
            }

            ifield {
                name = "By Staff"
                value = sourceMod
            }

            messages.reversed().forEach {
                val attachments = it.attachments
                val content = if (attachments.size > 0 && attachments[0].isImage())
                                  attachments[0].proxyUrl
                              else
                                  it.contentRaw

                field {
                    name = "Message"
                    value = "${it.author.asMention}: $content" // Can't mention in 'name'
                    inline = false
                }
            }

            setColor(Color.CYAN)
        }

private fun getTargets(msg: String): List<String> =
    if (msg.contains(",")) {
        msg.split(",")
    } else {
        listOf(msg)
    }
