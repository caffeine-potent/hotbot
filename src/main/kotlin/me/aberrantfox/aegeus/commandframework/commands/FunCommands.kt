package me.aberrantfox.aegeus.commandframework.commands

import khttp.get as kget
import me.aberrantfox.aegeus.commandframework.ArgumentType
import me.aberrantfox.aegeus.commandframework.CommandSet
import me.aberrantfox.aegeus.dsls.command.commands
import org.jsoup.Jsoup
import java.net.URLEncoder

import java.util.*

@CommandSet
fun funCommands() =
    commands {
        command("cat") {
            execute {
                val json = kget("http://random.cat/meow").jsonObject
                it.respond(json.getString("file"))
            }
        }
        
        command("bird") {
            execute {
                val json = kget("https://birdsare.cool/bird.json?exclude=webm,mp4").jsonObject
                it.respond(json.getString("url"))
            }
        }

        command("ball") {
            expect(ArgumentType.Sentence)
            execute {
                val query = it.args[0] as String
                val json = kget("https://8ball.delegator.com/magic/JSON/abc").jsonObject
                it.respond(json.getJSONObject("magic").getString("answer"))
            }
        }

        command("flip") {
            execute {
                val message = if (Random().nextBoolean()) "Heads" else "tails"
                it.respond(message)
            }
        }

        command("dog") {
            execute {
                val json = kget("https://dog.ceo/api/breeds/image/random").jsonObject
                it.respond(json.getString("message"))
            }
        }

        command("google") {
            expect(ArgumentType.Sentence)
            execute {
                val google = "http://www.google.com/search?q="
                val search = it.args[0] as String
                val charset = "UTF-8"
                val userAgent = "Mozilla/5.0"

                val links = Jsoup.connect(google + URLEncoder.encode(search, charset)).userAgent(userAgent).get().select(".g>.r>a")

                it.respond(links.first().absUrl("href"))
            }
        }
    }
