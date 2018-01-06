package me.aberrantfox.aegeus.dsls.embed

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed


class EmbedDSLHandle : EmbedBuilder() {
    operator fun invoke(args: EmbedDSLHandle.() -> Unit) {}

    fun title(title: String?) = this.setTitle(title)

    fun description(descr: String?) = this.setDescription(descr)

    fun field(construct: FieldStore.() -> Unit) {
        val field = FieldStore()
        field.construct()
        addField(field.name, field.value, field.inline)
    }

    fun ifield(construct: FieldStore.() -> Unit) {
        val field = FieldStore()
        field.construct()
        addField(field.name, field.value, true)
    }
}

data class FieldStore(var name: String? = "", var value: String? = "", var inline: Boolean = true)

fun embed(construct: EmbedDSLHandle.() -> Unit): MessageEmbed {
    val handle = EmbedDSLHandle()
    handle.construct()
    return handle.build()
}