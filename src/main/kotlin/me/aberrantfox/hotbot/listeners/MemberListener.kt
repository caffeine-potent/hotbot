package me.aberrantfox.hotbot.listeners

import me.aberrantfox.hotbot.logging.BotLogger
import me.aberrantfox.hotbot.services.Configuration
import me.aberrantfox.hotbot.services.MessageService
import me.aberrantfox.hotbot.services.MessageType
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.awt.Color



class MemberListener(val configuration: Configuration, val logger: BotLogger) : ListenerAdapter() {

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val target = event.guild.textChannels.findLast { it.id == configuration.messageChannels.welcomeChannel }
        val response = MessageService.getMessage(MessageType.Join).replace("%name%", event.user.asMention)
        val userImage = event.user.effectiveAvatarUrl

        target?.sendMessage(buildJoinMessage(response, userImage))?.queue {
            it.addReaction("\uD83D\uDC4B").queue()
        }
    }

    override fun onGuildMemberLeave(e: GuildMemberLeaveEvent) = logger.info("${e.user.asMention} left the server")
}

private fun buildJoinMessage(response: String, image: String) =
        EmbedBuilder()
                .setTitle("Player Get!")
                .setDescription(response)
                .setColor(Color.red)
                .setThumbnail(image)
                .addField("How do I start?",
                         "Take a read of #faq, alongside #rules-and-info. When you are done, scan up and down the " +
                                 "channel list. Don't forget to say hi ;).", false)
                .build()

