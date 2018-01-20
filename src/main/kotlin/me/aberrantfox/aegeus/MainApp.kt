package me.aberrantfox.aegeus

import me.aberrantfox.aegeus.commandframework.produceContainer
import me.aberrantfox.aegeus.database.loadUpManager
import me.aberrantfox.aegeus.extensions.hasRole
import me.aberrantfox.aegeus.extensions.timeToDifference
import me.aberrantfox.aegeus.extensions.unmute
import me.aberrantfox.aegeus.listeners.*
import me.aberrantfox.aegeus.listeners.antispam.DuplicateMessageListener
import me.aberrantfox.aegeus.listeners.antispam.InviteListener
import me.aberrantfox.aegeus.logging.convertChannels
import me.aberrantfox.aegeus.services.*
import me.aberrantfox.aegeus.database.setupDatabaseSchema
import me.aberrantfox.aegeus.permissions.PermissionManager
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Guild


fun main(args: Array<String>) {
    println("Starting to load hotbot.")
    val container = produceContainer()
    val config = loadConfig() ?: return


    saveConfig(config)
    setupDatabaseSchema(config)

    val jda = JDABuilder(AccountType.BOT).setToken(config.serverInformation.token).buildBlocking()
    val logger = convertChannels(config.logChannels, jda)

    logger.info("connected")
    val mutedRole = jda.getRolesByName(config.security.mutedRole, true).first()
    val tracker = MessageTracker(1)
    val guild = jda.getGuildById(config.serverInformation.guildid)
    val manager = PermissionManager(HashMap(), guild.roles, guild, config)

    loadUpManager(manager)

    container.newLogger(logger)

    jda.addEventListener(
            CommandListener(config, container, jda, logger, guild, manager),
            MemberListener(config, logger),
            InviteListener(config, logger),
            MentionListener(config, jda.selfUser.name),
            VoiceChannelListener(logger),
            NewChannelListener(mutedRole),
            DuplicateMessageListener(config, logger, tracker),
            RoleListener(config))

    jda.presence.setPresence(OnlineStatus.ONLINE, Game.of("${config.serverInformation.prefix}help"))
    jda.guilds.forEach { setupMutedRole(it, config.security.mutedRole) }

    handleLTSMutes(config, jda)
    logger.info("Fully setup, now ready for use.")
}

private fun setupMutedRole(guild: Guild, roleName: String) {
    if (!guild.hasRole(roleName)) {
        guild.controller.createRole().setName(roleName).queue {
            handleRole(guild, roleName)
        }
        return
    }

    handleRole(guild, roleName)
}

private fun handleRole(guild: Guild, roleName: String) {
    val role = guild.getRolesByName(roleName, true).first()

    guild.textChannels.forEach {
        val hasOverride = it.permissionOverrides.any {
            it.role.name.toLowerCase() == roleName.toLowerCase()
        }

        if (!hasOverride) it.createPermissionOverride(role).setDeny(Permission.MESSAGE_WRITE).queue()
    }
}

private fun handleLTSMutes(config: Configuration, jda: JDA) {
    config.security.mutedMembers.forEach {
        val difference = timeToDifference(it.unmuteTime)
        val guild = jda.getGuildById(it.guildId)
        val user = guild.getMemberById(it.user)

        if(user != null) {
            unmute(guild, user.user, config, difference, it)
        }
    }
}
