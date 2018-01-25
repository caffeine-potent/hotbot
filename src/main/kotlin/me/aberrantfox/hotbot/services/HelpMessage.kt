package me.aberrantfox.hotbot.services

import com.google.gson.Gson
import me.aberrantfox.hotbot.dsls.command.CommandsContainer
import java.io.File


data class HelpFile(val commands: List<CommandDescriptor>, val categoryDescriptions: Map<String, String>)

data class CommandDescriptor(val name: String,
                             val description: String,
                             val structure: String?,
                             val example: String?,
                             val category: String)

enum class SelectionArgument { CommandName, CategoryName }

object HelpConf {
    val configuration: HelpFile

    init {
        val data = File("help.json").readText()
        val gson = Gson()

        configuration = gson.fromJson(data, HelpFile::class.java)
    }

    fun fetchArgumentType(value: String): SelectionArgument? {
        val isCategory = configuration.commands.any { it.category.toLowerCase() == value.toLowerCase() }

        if(isCategory) return SelectionArgument.CategoryName

        val isCommandName = configuration.commands.any { it.name.toLowerCase() == value.toLowerCase() }

        if(isCommandName) return SelectionArgument.CommandName

        return null
    }

    fun hasHelp(name: String) = HelpConf.configuration.commands
        .map { it.name }
        .map { it.toLowerCase() }
        .all { it != name.toLowerCase() }

    fun fetchCommandsInCategory(category: String): String =
            HelpConf.configuration.commands
                    .filter { it.category.toLowerCase() == category.toLowerCase() }
                    .map { it.name }
                    .reduceRight {a, b -> "$a, $b" }

    fun fetchCommandDescriptor(command: String) =
            HelpConf.configuration.commands.findLast { it.name.toLowerCase() == command.toLowerCase() }

    fun fetchCategories() =
            HelpConf.configuration.commands
                    .map { it.category }
                    .toSet()
                    .reduce { a, b ->  "$a, $b"}

    fun getDocumentationErrors(container: CommandsContainer): List<String> {
        val errors = ArrayList<String>()
        val commandNames = container.commands.keys.map { it.toLowerCase() }

        val docCommands = configuration.commands
        val docCommandNames = docCommands.map { it.name.toLowerCase() }

        val duplicates = docCommands.groupBy { it.name.toLowerCase() }.filter { it.value.size > 1 }
        if (duplicates.isNotEmpty()) {
            errors.add("Duplicate Commands: ${duplicates.keys.joinToString(", ")}")
        }

        val undocumentedCommands = commandNames.filterNot { docCommandNames.contains(it) }
        if (undocumentedCommands.isNotEmpty()) {
            errors.add("Undocumented Commands: ${undocumentedCommands.joinToString(", ")}")
        }

        val unknownCommands = docCommandNames.filterNot { commandNames.contains(it) }
        if (unknownCommands.isNotEmpty()) {
            errors.add("Unknown Commands: ${unknownCommands.joinToString(", ")}")
        }

        return errors
    }
}